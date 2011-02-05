package org.apelikecoder.bgdictum;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LinksFinder {
    public static int LATIN = 0;
    public static int CYRILLIC = 1;
    public static int TRANSCRIPTION = 2;
    private static final Pattern WORD_MATCHER = Pattern.compile(
                          "\\b[a-zA-Z]"             //
                        + "{1,}+\\b");              //at least 2 chars long
    private static final Pattern WORD_MATCHER2 = Pattern.compile(
            "\\b["
            + '\u0430' + '-' + '\u044f' //cyrillic a-ya
            + '\u0410' + '-' + '\u042f' //cyrillic A-Ya
            + "]"                       //
            + "{1,}+\\b");              //
    private static final Pattern TRANSCRIPTION_MATCHER = Pattern.compile(
            "\\[.*\\]"
            );

    private static HashMap<Pattern, Integer> TYPES = new HashMap<Pattern, Integer>();
    static {
        TYPES.put(WORD_MATCHER, LATIN);
        TYPES.put(WORD_MATCHER2, CYRILLIC);
        TYPES.put(TRANSCRIPTION_MATCHER, TRANSCRIPTION);
    }
    public static final ArrayList<LinkSpec> getLinks(String text) {
        ArrayList<LinkSpec> links = new ArrayList<LinkSpec>();
        gatherLinks(links, text, WORD_MATCHER);
        gatherLinks(links, text, WORD_MATCHER2);
        gatherLinks(links, text, TRANSCRIPTION_MATCHER);
        return links;
    }

    private static final void gatherLinks(ArrayList<LinkSpec> links, String s, Pattern pattern) {
        Matcher m = pattern.matcher(s);
        int type = TYPES.get(pattern);
        while (m.find()) {
            int start = m.start();
            int end = m.end();

            LinkSpec spec = new LinkSpec();
            String url = m.group(0);
            spec.url = url;
            spec.start = start;
            spec.end = end;
            spec.type = type;
            links.add(spec);
        }
        int len = links.size();
        int i = 0;

        while (i < len - 1) {
            LinkSpec a = links.get(i);
            LinkSpec b = links.get(i + 1);
            int remove = -1;

            if (a.url.equals(b.url)) {
                remove = i + 1;
                if (remove != -1) {
                    links.remove(remove);
                    len--;
                    continue;
                }
            }
            i++;
        }
    }

    public static class LinkSpec {
        public String url;
        public int start;
        public int end;
        public int type;
    }

    public static int getType(String text) {
        if (WORD_MATCHER.matcher(text).find())
            return TYPES.get(WORD_MATCHER);
        if (WORD_MATCHER2.matcher(text).find())
            return TYPES.get(WORD_MATCHER2);
        return TYPES.get(TRANSCRIPTION_MATCHER);
    }
}
