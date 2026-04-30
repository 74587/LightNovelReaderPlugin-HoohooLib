package io.limao996.hoohoolib.utils

object FontDecoder {
    private const val z = "的一是了我不人在他有这个上们来到时大地为子中你说生国年着就那和要她出也得里后自以会家可下而过天去能对小多然于心学么之都好看起当发没成只如事把还用第样道想作种开美总从无情己面最女但现前些所同日手又行意动"
    private const val startCodePoint = 0xE800

    fun decode(text: String): String {
        return text.map { ch ->
            val codePoint = ch.code
            val index = codePoint - startCodePoint
            if (index in z.indices) {
                z[index]
            } else {
                ch
            }
        }.joinToString("")
    }
}
