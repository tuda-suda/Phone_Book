package phonebook
import java.io.File
import kotlin.math.sqrt

var g_linearFound = 0
var g_linearSearchTime: Long = 0
var g_sortTooLong = false

class Time (millis: Long) {
    var min = millis / 1000 / 60
    var sec = millis / 1000 % 60
    var ms = millis % 1000
}

fun printStat(found: Int,
              total: Int,
              totalTime: Time,
              linearSearch: Boolean = false,
              sortTime: Time = Time(0),
              searchTime: Time = Time(0),
              hasSwitched: Boolean = false,
              hashTable: Boolean = false) {
    println("Found $found / $total entries. Time taken: ${totalTime.min} min. ${totalTime.sec} sec. ${totalTime.ms} ms.")
    if (!linearSearch) {
        if (hashTable) {
            println("Creating time: ${sortTime.min} min. ${sortTime.sec} sec. ${sortTime.ms} ms.")
        } else {
            print("Sorting time: ${sortTime.min} min. ${sortTime.sec} sec. ${sortTime.ms} ms.")
            if (hasSwitched) {
                println(" - STOPPED, moved to linear search")
            } else print("\n")
        }
        println("Searching time: ${searchTime.min} min. ${searchTime.sec} sec. ${searchTime.ms} ms.")
    }
}

fun nameFromLine(line: String, number: Boolean = false): String{
    return if (number) {
        Regex("[a-zA-Z]*").replace(line, "").trim()
    } else Regex("[0-9]*").replace(line, "").trim()
}

fun sortBubble(array: MutableList<String>): MutableList<String>{
    val n = array.size
    val runStartTime = System.currentTimeMillis()

    for (i in 0 until n - 1){
        for (j in 0 until n - i - 1) {
            if (System.currentTimeMillis() - runStartTime > 10* g_linearSearchTime) {
                g_sortTooLong = true
                return array
            }
            if (nameFromLine(array[j]) > nameFromLine(array[j + 1])) {
                val inter = array[j]
                array[j] = array[j + 1]
                array[j + 1] = inter
            }
        }
    }
    return array
}

fun qspartition(array: MutableList<String>, startIndex: Int, endIndex: Int): Int {
    val pivot = nameFromLine(array[endIndex])

    var i = startIndex - 1 //index of smaller element

    for (j in startIndex..endIndex) {
        if (nameFromLine(array[j]) < pivot) {
            i++
            val inter = array[j]
            array[j] = array[i]
            array[i] = inter
        }
    }

    val inter = array[i + 1]
    array[i + 1] = array[endIndex]
    array[endIndex] = inter

    return i + 1
}

fun quickSort(array: MutableList<String>, startIndex: Int, endIndex: Int) {
    if (startIndex < endIndex) {
        val partIndex = qspartition(array, startIndex, endIndex)

        quickSort(array, startIndex, partIndex - 1)
        quickSort(array, partIndex + 1, endIndex)
    }
}

fun linearSearch(contactsList: MutableList<String>, searchList: MutableList<String>, switch: Boolean = false) {
    var found: Int
    val foundNumbersList = arrayListOf<String>()
    val startTime = System.currentTimeMillis()

    for (name in searchList){
        for (number in contactsList){
            if (number.contains(name)){
                //println("found! $found / 500 $name")
                foundNumbersList.add(name)
            }
        }
    }
    found = foundNumbersList.toSet().size
    g_linearFound = found
    val timeTakenMillis = System.currentTimeMillis() - startTime
    val timeTaken = Time(timeTakenMillis)
    if (!switch) {
        printStat(found, searchList.size, timeTaken, true)
    }
    g_linearSearchTime = timeTakenMillis
}

fun jumpSearch(contactsList: MutableList<String>, searchList: MutableList<String>){
    val startTime = System.currentTimeMillis()
    val sortedContactsList = sortBubble(contactsList)
    val sortTimeTaken = Time(System.currentTimeMillis() - startTime)
    val searchStartTime = System.currentTimeMillis()
    val searchingTime: Time
    var found: Int

    if (g_sortTooLong){
        linearSearch(contactsList, searchList, true)
        found = g_linearFound
        searchingTime = Time(g_linearSearchTime)
    } else {
        val foundNumbersList = mutableSetOf<String>()
        val n = sqrt(sortedContactsList.size.toDouble()).toInt()

        for (name in searchList) {
            for (i in 0 until sortedContactsList.size step n) {
                if (sortedContactsList[i].contains(name)) {
                    foundNumbersList.add(name)
                } else if(nameFromLine(sortedContactsList[i]) > name) {
                    for (j in i downTo i - n) {
                        while (j > sortedContactsList.lastIndex){
                            continue
                        }
                        //println("$j, $i")
                        if (sortedContactsList[j].contains(name)) {
                            foundNumbersList.add(name)
                        }
                    }
                }
                //if (found == searchList.size) break
            }
        }
        found = foundNumbersList.size
        searchingTime = Time(System.currentTimeMillis() - searchStartTime)
    }
    val timeTaken = Time(System.currentTimeMillis() - startTime)

    printStat(found, searchList.size, timeTaken, false, sortTimeTaken, searchingTime, g_sortTooLong)
}

fun binarySearch(contactsList: MutableList<String>, searchList: MutableList<String>) {
    val startTime = System.currentTimeMillis()
    quickSort(contactsList, 0, contactsList.lastIndex)
    val sortTimeTaken = Time(System.currentTimeMillis() - startTime)
    val foundNumbersList = mutableSetOf<String>()

    val searchStartTime = System.currentTimeMillis()
    for (name in searchList) {
        var left = 0
        var right = contactsList.lastIndex
        while (left <= right) {
            val middle = left + (right - left) / 2

            if (contactsList[middle].contains(name)) {
                foundNumbersList.add(name)
                break
            } else if (name > nameFromLine(contactsList[middle])) {
                left = middle + 1
            } else {
                right = middle - 1
            }
        }
    }

    val searchTime = Time(System.currentTimeMillis() - searchStartTime)
    val timeTaken = Time(System.currentTimeMillis() - startTime)

    printStat(foundNumbersList.size, searchList.size, timeTaken, false, sortTimeTaken, searchTime)
}

fun hashTableSearch(contactsList: MutableList<String>, searchList: MutableList<String>) {
    val startTime = System.currentTimeMillis()
    val hashTable = hashMapOf<String, String>()
    for (line in contactsList) {
        hashTable[nameFromLine(line)] = nameFromLine(line, true)
    }
    val creationTime = Time(System.currentTimeMillis() - startTime)

    val searchTimeStart = System.currentTimeMillis()
    val foundNumbersList = mutableSetOf<String>()
    for (name in searchList) {
        if (hashTable.containsKey(name)) foundNumbersList.add(name)
    }
    val searchTime = Time(System.currentTimeMillis() - searchTimeStart)
    val timeTaken = Time(System.currentTimeMillis() - startTime)

    printStat(foundNumbersList.size, searchList.size, timeTaken, false, creationTime, searchTime, false, true)
}

fun main(argv: Array<String>) {
    val directory = argv[0]
    val find = argv[1]

    println("Start searching (linear search)...")
    linearSearch(File(directory).readLines().toMutableList(), File(find).readLines().toMutableList())
    println("Start searching (bubble sort + jump search)...")
    jumpSearch(File(directory).readLines().toMutableList(), File(find).readLines().toMutableList())
    println("Start searching (quick sort + binary search)...")
    binarySearch(File(directory).readLines().toMutableList(), File(find).readLines().toMutableList())
    println("Start searching (hash table)...")
    hashTableSearch(File(directory).readLines().toMutableList(), File(find).readLines().toMutableList())
}
