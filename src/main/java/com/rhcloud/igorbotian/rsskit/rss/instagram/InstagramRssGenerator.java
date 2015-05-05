package com.rhcloud.igorbotian.rsskit.rss.instagram;

import com.rhcloud.igorbotian.rsskit.rest.instagram.InstagramException;
import com.rhcloud.igorbotian.rsskit.rest.instagram.InstagramFeed;
import com.rhcloud.igorbotian.rsskit.rest.instagram.InstagramPost;
import com.rometools.rome.feed.synd.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Igor Botian <igor.botian@gmail.com>
 */
public class InstagramRssGenerator {

    public SyndFeed generate(InstagramFeed feed) throws InstagramException {
        Objects.requireNonNull(feed);

        SyndFeed rss = new SyndFeedImpl();
        rss.setTitle("Instagram self feed");
        rss.setLink("http://www.instagram.com");
        rss.setFeedType("rss_2.0");
        rss.setDescription(rss.getTitle());
        rss.setPublishedDate(new Date());
        rss.setEntries(generateEntries(feed.posts));

        return rss;
    }

    private List<SyndEntry> generateEntries(List<InstagramPost> posts) {
        assert posts != null;

        List<SyndEntry> entries = new ArrayList<>(posts.size());

        for(InstagramPost post : posts) {
            entries.add(generateEntry(post));
        }

        return entries;
    }

    private SyndEntry generateEntry(InstagramPost post) {
        assert post != null;

        SyndEntry entry = new SyndEntryImpl();
        String author = author(post.author.fullName, post.author.username);

        entry.setAuthor(author);
        entry.setPublishedDate(post.date);
        entry.setTitle(author);
        entry.setLink(post.url);
        entry.setDescription(generateDescription(post.comment, post.thumbnailURL));

        return entry;
    }

    private String author(String fullName, String username) {
        assert fullName != null;
        assert username != null;

        return String.format("%s (@%s)", fullName, username);
    }

    private SyndContent generateDescription(String comment, String mediaURL) {
        assert comment != null;
        assert mediaURL != null;

        SyndContent content = new SyndContentImpl();
        content.setType("text/html");

        StringBuilder builder = new StringBuilder(comment);

        if(builder.length() > 0) {
            builder.append("<br/><br/>");
        }

        builder.append(String.format("<img src='%s'/>", mediaURL));
        content.setValue(builder.toString());

        return content;
    }
}