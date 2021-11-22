package edu.opjms.candidateSelector

import net.kilobyte1000.Houses
import edu.opjms.common.Posts
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.*

@OptIn(ExperimentalPathApi::class)
internal class ListData(path: Path? = null) {

    private val candidateList = if (path != null) {

        val input = DataInputStream(path.inputStream().buffered())

        input.use {
            //used for fast verification, not used
            it.readBoolean()
            List(Houses.SIZE) { _ ->
                val houseMaleList = readList(it)
                val houseFemaleList = readList(it)
                val sportsMaleList = readList(it)
                val sportsFemaleList = readList(it)
                val housePosts = GenderedPost(houseMaleList, houseFemaleList)
                val sportsPost = GenderedPost(sportsMaleList, sportsFemaleList)
                PostPair(housePosts, sportsPost)
            }
        }

    } else {
        List(Houses.SIZE) { PostPair() }
    }

    fun getList(houses: Houses, post: Posts): GenderedPost {
        return if (post == Posts.HOUSE_PREFECT)
            candidateList[houses.ordinal].housePrefects
        else
            candidateList[houses.ordinal].sportsPrefects
    }


    @OptIn(ExperimentalPathApi::class)
    fun writeToFile(path: Path) {
        if (path.notExists()) {
            try {
                path.createDirectories()
            } catch (ignore: IOException) {
            } // file exists
        }

        val out = path.outputStream().buffered()

        var isEmpty = false
        for (posts in candidateList) {
            if (posts.housePrefects.maleList.isEmpty() || posts.housePrefects.femaleList.isEmpty()
                || posts.sportsPrefects.maleList.isEmpty() || posts.sportsPrefects.femaleList.isEmpty()
            ) {
                isEmpty = true
                break
            }
        }
        //used for fast verification if file is OK for use
        DataOutputStream(out).use {
            it.writeBoolean(isEmpty)
            for (pair in candidateList) {
                val (housePrefects, sportPrefects) = pair
                listOf(
                    housePrefects.maleList,
                    housePrefects.femaleList,
                    sportPrefects.maleList,
                    sportPrefects.femaleList
                ).forEach { list ->
                    it.writeInt(list.size)
                    for (item in list) {
                        it.writeUTF(item)
                    }
                }
            }
        }

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ListData

        if (candidateList != other.candidateList) return false

        return true
    }

    override fun hashCode(): Int {
        return candidateList.hashCode()
    }


    private fun readList(input: DataInputStream): List<String> {

        val size = input.readInt()
        val list = ArrayList<String>(size)

        repeat(size) {
            val item = input.readUTF()
            list.add(item)
        }
        list.sort()

        // comparision with adjacent strings
        // is enough to check for duplicates in a
        // sorted list
        for (i in size - 1 downTo 1) {
            val name = list[i]
            if (name.isBlank() || name.equals(list[i - 1], true))
                list.removeAt(i)
        }
        return list
    }
}

data class PostPair(
    val housePrefects: GenderedPost = GenderedPost(),
    val sportsPrefects: GenderedPost = GenderedPost()
)

class GenderedPost(maleList: List<String> = mutableListOf(), femaleList: List<String> = mutableListOf()) {
    val maleList: ObservableList<String> = FXCollections.observableArrayList(maleList)
    val femaleList: ObservableList<String> = FXCollections.observableArrayList(femaleList)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GenderedPost

        if (maleList != other.maleList) return false
        if (femaleList != other.femaleList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = maleList.hashCode()
        result = 31 * result + femaleList.hashCode()
        return result
    }

    override fun toString(): String {
        return "GenderedPost(maleList=$maleList, femaleList=$femaleList)"
    }


}