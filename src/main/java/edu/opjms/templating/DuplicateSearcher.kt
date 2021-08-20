package edu.opjms.templating

interface DuplicateSearcher {
    fun searchDuplicates(value: String, itr: Iterator<String>, size: Int): IntArray
}