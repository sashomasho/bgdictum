package org.apelikecoder.bgdictum;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LinksFinder {
    public static final Pattern WORD_MATCHER = Pattern.compile(
                          "\\b[a-zA-Z" 
                        + '\u0430' + '-' + '\u044f' //cyrillic a-ya 
                        + '\u0410' + '-' + '\u042f' //cyrillic A-Ya
                        + ']'                       //
                        + "{3,}+\\b");              //at least 3 chars long

    public static final ArrayList<LinkSpec> getLinks(String text) {
        ArrayList<LinkSpec> links = new ArrayList<LinkSpec>();
        gatherLinks(links, text);
        return links;
    }

    private static final void gatherLinks(ArrayList<LinkSpec> links, String s) {
        Matcher m = WORD_MATCHER.matcher(s);

        while (m.find()) {
            int start = m.start();
            int end = m.end();

            LinkSpec spec = new LinkSpec();
            String url = m.group(0);
            spec.url = url;
            spec.start = start;
            spec.end = end;
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
    }
}
