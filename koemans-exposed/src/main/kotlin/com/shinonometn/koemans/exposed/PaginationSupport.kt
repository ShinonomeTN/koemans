package com.shinonometn.koemans.exposed

import com.shinonometn.koemans.utils.UrlParameters
import com.shinonometn.koemans.utils.isNumber
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import kotlin.math.ceil

/**
 * Represent a page request
 * @param page is page index, start from 0
 * @param size is page size
 */
class PageRequest(val page: Long = 0, val size: Long = 20) {
    val offset: Long
        get() = page * size

    fun <T> toPage(total: Long, hasNext: Boolean, content: Collection<T>): Page<T> {
        return Page(offset, size, total, hasNext, content)
    }

    companion object
}

/** Build page request directly from url parameters */
fun PageRequest.Companion.from(parameters: UrlParameters): PageRequest {
    val page = parameters["page"]?.firstOrNull()?.takeIf { it.isNumber() }?.toLong() ?: 0
    val size = parameters["size"]?.firstOrNull()?.takeIf { it.isNumber() && it.toInt() > 0 }?.toLong() ?: 20
    return PageRequest(page, size)
}

/**
 * Exposed extension that applying paging on Query
 */
fun <T> Query.pagingBy(pageRequest: PageRequest, countQuery: Boolean = true, converter: (ResultRow) -> T): Page<T> {
    val query = this

    // If request count query then get total element count
    val count = if (countQuery) query.count() else -1

    val hasNext = when {
        // Compute it if countQuery enabled
        countQuery -> count > (pageRequest.offset + pageRequest.size)
        // or else make a prefetch query
        else -> query.limit(1, pageRequest.offset + pageRequest.size).count() > 0
    }

    val result = query.limit(pageRequest.size.toInt(), pageRequest.offset).map(converter)

    return pageRequest.toPage(count, hasNext, result)
}

/**
 * Pagination query result
 * @param offset is item offset in query result
 * @param size is query record amount limitation for each page
 * @param total is total item count in database (or query). if [total] == -1 means counting have been disabled.
 * @param hasNext is a hint for if the query result has next page
 * @param content is content of this page
 */
class Page<T>(val offset: Long, val size: Long, val total: Long, val hasNext: Boolean, val content: Collection<T>) {
    /**
     * Computed current page info
     * Start from 0
     */
    val currentPageIndex: Long
        get() = ceil(offset / size.toDouble()).toLong()

    /**
     * Computed total page info
     * return 0 if unknown
     */
    val totalPageCount: Long
        get() = if (total >= 0) ceil(total / size.toDouble()).toLong() else 0

    /**
     * Computed hint of is first page
     */
    val hasPrev: Boolean
        get() = currentPageIndex > 0

    /**
     * Is the pagination query enable total element counting.
     */
    val hasTotalItemQuery: Boolean
        get() = total >= 0

    fun <R> convert(converter: (T) -> R): Page<R> {
        return Page(offset, size, total, hasNext, content.map(converter))
    }

    val isEmpty : Boolean
        get() = content.isEmpty()
}