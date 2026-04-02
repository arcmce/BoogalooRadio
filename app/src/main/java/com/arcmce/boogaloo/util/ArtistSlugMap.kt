package com.arcmce.boogaloo.util

/**
 * Maps schedule show names (playlist.name from the radio.co API) to their Mixcloud playlist slugs.
 * Keys are the playlist.name values (the specific show/DJ identifier shown as secondary text
 * in the schedule panel); values are Mixcloud slugs for navigating to that artist's mix archive.
 * Re-derived 2026-04-02 by cross-referencing schedule playlist.name + playlist.artist values
 * against https://api.mixcloud.com/BoogalooRadio/playlists/.
 *
 * Unmatched schedule names (no Mixcloud playlist found):
 *   Amelie & Timothy, Astile Doherty, Bing Lewis, Christos Lawton, Darren Walker,
 *   Dave Ashby, Jody and Daisy, Kieran Smyth, Raechel Donahue,
 *   Stevie Windows Al & Indie, Tia O'Donnell, Tom Raine
 */
object ArtistSlugMap {

    val map: Map<String, String> = mapOf(
        // Exact name matches
        "Adam Wedd"                      to "adam-wedd",
        "Alastair Shuttleworth"          to "alastair-shuttleworth",
        "Cherry Red Records"             to "cherry-red-records",
        "Deb & Jules - Turn It Up Darling" to "deb-jules-turn-it-up-darling",
        "HANC"                           to "hanc",
        "Jim Fry"                        to "jim-fry",
        "Joy Doc"                        to "joy-doc",
        "Kelly Jade"                     to "kelly-jade",
        // Derived from playlist.artist tagline or partial name match
        "Cloe Lee"                       to "bonanza",               // artist: "Giddy up for Bonanza!"
        "Henry Shaw"                     to "henry-shaw-aka-dancefloor-troubadour", // artist: "Dancefloor Troubadour"
        "Jack Young & Lewis Evans"       to "jack-young-lewis-evans", // MC: "Jack Young & Lewis Evans present the Blue Harbour"
        "Jeremy Thoms"                   to "radio-activity",         // artist: "Radio Activity"
        "Les Miserable & J Dangerous"    to "les-miserables-singles-club", // MC: "Les Miserable"
        "Tilly Bartelt"                  to "tune-in-with-tilly",     // MC: "Tune in with Tilly"
        "Tonn Éalú"                      to "tonn-éalú",              // schedule name matches MC slug
        "Wayne Gormally"                 to "trailers-wayne-john-george", // artist: "Trailers Movie show"
    )

    fun slugFor(artistName: String): String = map[artistName] ?: ""
}
