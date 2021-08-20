package edu.opjms.templating

fun searchString(value: String, itr: Iterator<String>, size: Int): IntArray {
    val indices = IntArray(size)
    var i = 0
    var lastIndex = 0

    for (s in itr) {
        if (s.equals(value, true))
            indices[lastIndex++] = i
        i++
    }

    return indices.copyOf(lastIndex)
}