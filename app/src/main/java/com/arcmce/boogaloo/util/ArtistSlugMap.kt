package com.arcmce.boogaloo.util

/**
 * Maps schedule artist names (from the radio.co API) to their Mixcloud playlist slugs.
 * Schedule uses show taglines in the artist field; this bridges them to Mixcloud.
 * Derived by cross-referencing schedule artist+show fields against Mixcloud playlist names.
 */
object ArtistSlugMap {

    val map: Map<String, String> = mapOf(
        "Adam Wedd"                                                  to "adam-wedd",
        "All the latest Irish releases."                             to "tonn-\u00e9al\u00fa",
        "Cherry Red Records"                                         to "cherry-red-records",
        "Dancefloor Troubadour"                                      to "henry-shaw-aka-dancefloor-troubadour",
        "Drum and Bass Show"                                         to "joy-doc",
        "ft. Tears at the Table"                                     to "tune-in-with-tilly",
        "Giddy up for Bonanza!"                                      to "bonanza",
        "Guitars, games and gran\u2019s cheese and pickle sandwiches" to "jack-young-lewis-evans",
        "Late Night With"                                            to "hanc",
        "Loads of new music for you!"                                to "bing",
        "Mates, Pints, Music"                                        to "kelly-jade",
        "music from the outer reaches..."                            to "alastair-shuttleworth",
        "Radio Activity"                                             to "radio-activity",
        "Radio On"                                                   to "jim-fry",
        "Trailers Movie show"                                        to "trailers-wayne-john-george",
        "verything from punk to 60s pop! Join in for the pub quiz, and listen out for J Dangerous\u2019 spooky story..." to "les-miserables-singles-club",
    )

    fun slugFor(artistName: String): String = map[artistName] ?: ""
}
