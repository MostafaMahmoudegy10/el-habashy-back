package com.example.elhabashyback.listing.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListingSearchQueryTests {

    @Test
    void normalizesArabicVariantsDiacriticsAndTatweel() {
        assertEquals("العقاريه في اسيوط", ListingSearchQuery.normalize("  العَقَارِيَّـة في أُسْيُوط  "));
    }

    @Test
    void buildsSafePrefixQueryWithDistinctBoundedTerms() {
        assertEquals(
                "private:* & gard:* & cairo:*",
                ListingSearchQuery.toPrefixTsQuery("Private, gard private Cairo!")
        );
    }

    @Test
    void ignoresPunctuationOnlyInput() {
        assertEquals("", ListingSearchQuery.toPrefixTsQuery("!!! --"));
    }

    @Test
    void expandsArabicTermsToCommonAttachedPrefixes() {
        assertEquals(
                "(حديقه:* | بحديقه:* | وحديقه:* | الحديقه:* | بالحديقه:* | والحديقه:*)",
                ListingSearchQuery.toPrefixTsQuery("حَدِيقَة")
        );
    }
}
