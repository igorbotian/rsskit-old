package com.rhcloud.igorbotian.rsskit.championat;

import com.rhcloud.igorbotian.rsskit.filter.RssDescriptionExtender;
import com.rhcloud.igorbotian.rsskit.filter.RssFilter;
import com.rhcloud.igorbotian.rsskit.filter.RssLinkMapper;
import com.rhcloud.igorbotian.rsskit.mobilizer.Mobilizers;
import com.rhcloud.igorbotian.rsskit.proxy.HttpLinkMapper;
import com.rhcloud.igorbotian.rsskit.utils.RssFeedUtils;
import com.rometools.rome.feed.synd.SyndFeed;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Igor Botian <igor.botian@gmail.com>
 */
public class ChampionatFilter implements RssFilter {

    private static final RssFilter breakingNewsFilter = new BreakingNewsFilter();
    private static final RssLinkMapper mobileVersionLinkMapper = new RssLinkMapper(new MobileVersionLinkMapper());
    private static final RssFilter descriptionExtender = new RssDescriptionExtender(Mobilizers.instapaper());

    @Override
    public SyndFeed apply(SyndFeed original) {
        Objects.requireNonNull(original);

        try {
            SyndFeed feed = RssFeedUtils.clone(original);

            feed = breakingNewsFilter.apply(feed);
            feed = mobileVersionLinkMapper.apply(feed);
            feed = descriptionExtender.apply(feed);

            return feed;
        } catch (IOException e) {
            return original;
        }
    }

    private static class MobileVersionLinkMapper implements HttpLinkMapper {

        private static final Pattern DESKTOP_VERSION_NEWS_URL_FORMAT = Pattern.compile(".*\\.championat\\.com/.*/news-(\\d+)-.*");
        private static final String MOBILE_VERSION_NEWS_URL_PREFIX = "http://m.championat.com/news/sport/football/";

        @Override
        public URL map(URL url) throws URISyntaxException, MalformedURLException {
            Objects.requireNonNull(url);

            Matcher matcher = DESKTOP_VERSION_NEWS_URL_FORMAT.matcher(url.toString());

            if(!matcher.matches() || matcher.groupCount() < 1) {
                return url;
            }

            return new URL(MOBILE_VERSION_NEWS_URL_PREFIX + matcher.group(1));
        }
    }
}
