package com.example.godutch.utils

object SortingHelper {
    fun sortList(list: ArrayList<String>) : ArrayList<String> {
        val words = kotlin.collections.ArrayList<Word>()

        for (word in list) {
            val wordChars = word.toCharArray()
            var isDigit = Character.isDigit(wordChars[0])
            val parts = ArrayList<Part>()
            var partStr = ""
            for (i in wordChars.indices) {
                if (Character.isDigit(wordChars[i]) == isDigit) {
                    partStr += wordChars[i].toString()
                } else {
                    parts.add(
                        if (Character.isDigit(wordChars[i])) Part(
                            partStr,
                            Type.String
                        ) else Part(Integer.parseInt(partStr), Type.Int)
                    )
                    isDigit = !isDigit
                    partStr = ""
                    partStr += wordChars[i].toString()
                }
                if (i == wordChars.size - 1) {
                    parts.add(
                        if (Character.isDigit(wordChars[i])) Part(Integer.parseInt(partStr), Type.Int) else Part(
                            partStr,
                            Type.String
                        )
                    )
                }
            }
            words.add(Word(word, parts))
        }

        var maxPartCount = 0
        for (w in words) {
            if (w.parts.size > maxPartCount)
                maxPartCount = w.parts.size
        }
        for (w in words) {
            while (w.parts.size < maxPartCount) {
                w.parts.add(Part("", Type.String))
            }
        }

        for (i in maxPartCount - 1 downTo 0) {
            var strWord = java.util.ArrayList<Word>()
            var numWord =java.util.ArrayList<Word>()
            for (word in words) {
                if (word.parts[i].type == Type.String)
                    strWord.add(word)
                else
                    numWord.add(word)
            }

            words.clear()

            if(strWord.isNotEmpty()) {
                val strWordsList = strWord.sortedWith(compareBy { (it.parts[i].value as String) })
                words.addAll(strWordsList)
            }
            if(numWord.isNotEmpty()) {
                val numWordsList = numWord.sortedWith(compareBy { (it.parts[i].value as Int) })
                words.addAll(numWordsList)
            }

        }

        list.clear()
        for(word in words) {
            list.add(word.value)
        }

        return list
    }

}

class Part(var value: Any, var type: Type)

enum class Type {
    Int,
    String
}

class Word(var value: String, var parts: ArrayList<Part>)
