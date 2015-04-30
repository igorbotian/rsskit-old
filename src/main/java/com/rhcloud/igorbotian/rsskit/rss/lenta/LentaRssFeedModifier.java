package com.rhcloud.igorbotian.rsskit.rss.lenta;

import com.rhcloud.igorbotian.rsskit.rss.LinkMapper;
import com.rhcloud.igorbotian.rsskit.rss.RssDescriptionExtender;
import com.rhcloud.igorbotian.rsskit.rss.RssModifier;
import com.rhcloud.igorbotian.rsskit.rss.RssLinkMapper;
import com.rhcloud.igorbotian.rsskit.mobilizer.Mobilizers;
import com.rhcloud.igorbotian.rsskit.utils.RssFeedUtils;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Igor Botian <igor.botian@gmail.com>
 */
public class LentaRssFeedModifier implements RssModifier {

    private static final RssModifier byCategoriesFilter = new ByCategoriesFilter();
    private static final RssLinkMapper mobileVersionLinkMapper = new RssLinkMapper(new MobileVersionLinkMapper());
    private static final RssModifier descriptionExtender = new RssDescriptionExtender(Mobilizers.instapaper());

    @Override
    public SyndFeed apply(SyndFeed original) {
        Objects.requireNonNull(original);

        try {
            SyndFeed feed = RssFeedUtils.clone(original);

            feed = byCategoriesFilter.apply(feed);
            feed = mobileVersionLinkMapper.apply(feed);
            feed = descriptionExtender.apply(feed);

            return feed;
        } catch (IOException e) {
            return original;
        }
    }

    private static class MobileVersionLinkMapper implements LinkMapper {

        private static final String MOBILE_VERSION_DOMAIN = "m.lenta.ru";

        @Override
        public URL map(URL url) throws URISyntaxException, MalformedURLException {
            assert url != null;

            URIBuilder builder = new URIBuilder();
            builder.setScheme(url.getProtocol());

            if(url.getPort() > 0) {
                builder.setPort(url.getPort());
            }

            builder.setHost(MOBILE_VERSION_DOMAIN);
            builder.setPath(url.getFile());

            return builder.build().toURL();
        }
    }

    private static class ByCategoriesFilter implements RssModifier {

        private static final List<String> CATEGORIES = Arrays.asList("Экономика", "Бывший СССР");

        @Override
        public SyndFeed apply(SyndFeed feed) {
            Objects.requireNonNull(feed);
            feed.setEntries(filteredEntries(feed));
            return feed;
        }

        private List<SyndEntry> filteredEntries(SyndFeed feed) {
            assert feed != null;

            List<SyndEntry> filtered = new ArrayList<>();

            for(SyndEntry entry : feed.getEntries()) {
                if(isFilteredByCategory(entry)) {
                    filtered.add(entry);
                }
            }

            return filtered;
        }

        private boolean isFilteredByCategory(SyndEntry entry) {
            assert entry != null;

            for(SyndCategory category : entry.getCategories()) {
                if(CATEGORIES.contains(category.getName())) {
                    return true;
                }
            }

            return false;
        }
    }
}