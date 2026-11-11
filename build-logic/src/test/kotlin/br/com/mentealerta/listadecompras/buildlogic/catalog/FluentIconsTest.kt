package br.com.mentealerta.listadecompras.buildlogic.catalog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FluentIconsTest {
    @Test
    fun selectsTheColorSvgOfEachFolder() {
        val paths = listOf(
            "assets/Red apple/Color/red_apple_color.svg",
            "assets/Red apple/Flat/red_apple_flat.svg",
            "assets/Red apple/3D/red_apple_3d.png",
            "assets/Nail polish/Dark/Color/nail_polish_color_dark.svg",
            "assets/Nail polish/Default/Color/nail_polish_color_default.svg",
            "assets/Nail polish/Default/Flat/nail_polish_flat_default.svg",
            "assets/Only 3D/3D/only_3d.png",
        )
        assertEquals(
            mapOf(
                "Nail polish" to "assets/Nail polish/Default/Color/nail_polish_color_default.svg",
                "Red apple" to "assets/Red apple/Color/red_apple_color.svg",
            ),
            selectColorSvgPaths(paths),
        )
    }

    @Test
    fun fluentIconsFileRoundTrips() {
        val content = fluentIconsFileContent("abc123", listOf("Red apple", "Banana"))
        assertTrue("# Commit: abc123" in content)
        assertEquals(setOf("Banana", "Red apple"), parseFluentIconsFile(content.lines()))
    }

    @Test
    fun referencedIconsAreSectionsAndActiveProducts() {
        val sections = "id,name,order,fluent_icon\nproduce,Hortifrúti,1,Leafy green\nother,Outros,2,Shopping cart"
        val products = "id,name,section,order,icon,icon_fit,status,replaced_by,notes\n" +
            "banana,Banana,produce,10,fluent:Banana,exact,active,,\n" +
            "kiwi,Kiwi,produce,20,own:kiwi,exact,active,,\n" +
            "papaya,Mamão,produce,30,,,active,,\n" +
            "cherry,Cereja,produce,40,fluent:Cherries,exact,retired,,"
        assertEquals(setOf("Banana", "Leafy green", "Shopping cart"), referencedFluentIcons(sections, products))
    }

    @Test
    fun encodesEachPathSegment() {
        assertEquals(
            "assets/Red%20apple/Color/red_apple_color.svg",
            encodePath("assets/Red apple/Color/red_apple_color.svg"),
        )
    }
}
