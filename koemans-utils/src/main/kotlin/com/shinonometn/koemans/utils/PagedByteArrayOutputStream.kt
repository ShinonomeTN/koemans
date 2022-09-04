package com.shinonometn.koemans.utils

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.charset.Charset

/**
 * Create a ByteArrayOutputStream that buffer is dynamically allocated page by page.
 * Supporting quota, throws QuotaExcessException if excess.
 */
class PagedByteArrayOutputStream(
    /**
     * The quota of the output stream buffer
     * quota < 0 means no limit
     */
    val quota: Int = -1,
    /**
     * How large a page will be allocated when buffer capacity insufficient
     */
    val pageSize: Int = 32
) : ByteArrayOutputStream(0) {

    class Page<T>(next: Page<T>? = null, val bytes: T) {
        var next = next
            internal set
    }

    val rootPage = Page(bytes = ByteArray(if (quota == 0) 0 else pageSize))

    var currentPage: Page<ByteArray> = rootPage
        private set

    /**
     * Allocated page count
     */
    var pages = 1
        private set

    /**
     * Allocated size
     */
    val capacity: Int
        get() = pages * pageSize

    /**
     * How many bytes was written
     */
    var size: Int = 0
        private set

    /**
     * How many page are full
     */
    var fulledPages = 0
        private set

    /**
     * Current page's write position
     */
    var pagePosition = 0
        private set

    /**
     * Current chunk's available space
     */
    val pageAvailable: Int
        get() = pageSize - pagePosition

    init {
        // Check if quota is legal
        if (quota > MAX_SIZE) throw IllegalArgumentException("Quota out of range")
        if (quota != 0 && pageSize <= 0) throw IllegalArgumentException("Page size should greater than zero")
    }

    class QuotaExcessException(message : String) : Exception(message)

    /**
     * Check if quota still sufficient
     */
    private fun ensureQuota(requiredCapacity: Int) {
        if (quota < 0) return // Unlimited
        if (quota == 0 || requiredCapacity > quota) throw QuotaExcessException("Quota excess: $quota")
    }

    /**
     * Grow the buffer to required size
     */
    private fun allocate(size: Int): Page<ByteArray> {
        if (size <= 0) return currentPage

        var newPages = (size / pageSize)
        if (size % pageSize > 0) newPages++
        var pointer = currentPage
        while (newPages-- > 0) {
            val newPage = Page(null, ByteArray(pageSize))
            pointer.next = newPage
            pointer = newPage
            pages++
        }
        return currentPage.next!!
    }

    /**
     * Ensure capacity is sufficient
     */
    private fun ensureCapacityAndQuota(requiredCapacity: Int): Page<ByteArray> {
        ensureQuota(requiredCapacity)
        return allocate(requiredCapacity - capacity)
    }

    /**
     * Get current chunk.
     * if current chunk is full, move to next chunk.
     */
    private fun currentPageOrNext(): ByteArray {
        // If current chunk is not full, return it
        if (pagePosition < pageSize) return currentPage.bytes

        // If current chunk is full, move to next chunk and return it
        // If no chunk available, create one if quota sufficient
        currentPage = currentPage.next ?: ensureCapacityAndQuota(size + 1)

        pagePosition = 0
        fulledPages++
        return currentPage.bytes
    }

    /**
     * Try filling the current chunk.
     * It will align the writing progress to chunks
     * returns the bytes written
     */
    private fun fillAlignToPage(bytes: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        val currentPageBuffer = currentPageOrNext()
        val availableSpace = pageAvailable

        // If current chunk's capacity is sufficient, fill it and exit with 0
        if (availableSpace >= length) {
            System.arraycopy(bytes, offset, currentPageBuffer, pagePosition, length)
            pagePosition += length
            size += length
            return length
        }

        // Otherwise, fill remaining chunk's space and exit
        System.arraycopy(bytes, offset, currentPageBuffer, pagePosition, availableSpace)
        pagePosition += availableSpace
        size += availableSpace

        return availableSpace
    }

    override fun write(b: Int) {
        ensureCapacityAndQuota(size + 1)
        currentPageOrNext()[pagePosition++] = b.toByte()
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        if (quota in 0 until off) throw QuotaExcessException("Quota excess: $quota")
        if (off < 0 || len < 0 || off + len - b.size > 0) throw IndexOutOfBoundsException()
        ensureCapacityAndQuota(size + len)
        // Fill chunks until finish
        var writtenBytes = 0
        while (writtenBytes < len) writtenBytes += fillAlignToPage(b, writtenBytes, len - writtenBytes)
    }

    override fun writeTo(out: OutputStream) {
        if (size <= pageSize) return out.write(currentPage.bytes, 0, size)
        val pageAvailableSpace = pageAvailable
        val currentPagePosition = pagePosition

        var fulledPageSize = fulledPages * pageSize
        val overflow = when (pageAvailableSpace) {
            0 -> {
                fulledPageSize += pageSize
                false
            }

            pageSize -> {
                false
            }

            else -> true
        }

        // If current chunk is full, count it in
        if (pageAvailableSpace == pageSize) fulledPageSize += pageSize

        var visitor = rootPage
        var writePos = 0
        while (writePos < fulledPageSize) {
            out.write(visitor.bytes)
            writePos += pageSize

            visitor = visitor.next ?: break
        }

        if (overflow) out.write(visitor.bytes, 0, currentPagePosition)
    }

    override fun reset() {
        size = 0
        pagePosition = 0
        fulledPages = 0
        currentPage = rootPage
    }

    @Synchronized
    fun reset(reallocate: Boolean) {
        reset()
        if (reallocate) {
            rootPage.next = null
            pages = 1
        }
    }

    override fun toByteArray(): ByteArray {
        // If the first chunk haven't finished, just copy it.
        if (size <= pageSize) return currentPage.bytes.copyOf(size)

        val buffer = ByteArray(size)
        // Get current chunk's write position
        val currentPagePosition = pagePosition
        // Get filled chunks count
        var fulledPageSize = fulledPages * pageSize

        // If current chunk is full, count it in
        val overflow = when (pageAvailable) {
            0 -> {
                fulledPageSize += pageSize
                false
            }

            pageSize -> false
            else -> true
        }

        // Visit from root and copy chunks to buffer
        var visitor = rootPage
        var written = 0
        while (written < fulledPageSize) {
            System.arraycopy(visitor.bytes, 0, buffer, written, pageSize)
            written += pageSize

            visitor = visitor.next ?: break
        }

        // If there has additional data in the latest chunk, copy it to the end of buffer
        if (overflow) System.arraycopy(visitor.bytes, 0, buffer, written, currentPagePosition)

        return buffer
    }

    override fun size(): Int = size

    override fun toString(): String = String(toByteArray())

    override fun toString(charsetName: String?): String = String(toByteArray(), 0, size, Charset.forName(charsetName))

    companion object {
        /**
         * @see ByteArrayOutputStream
         */
        const val MAX_SIZE = Integer.MAX_VALUE - 8
    }
}