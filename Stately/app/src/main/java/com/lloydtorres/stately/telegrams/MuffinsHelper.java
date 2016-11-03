/**
 * Copyright 2016 Lloyd Torres
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lloydtorres.stately.telegrams;

/*

                                         __         __----__
                                        /  \__..--''    _-__''-_
                                       ( /  \            `-.''''`
                                       | |   `-..__  .,     `.
                         ___           ( '.  \ ____`\ )`-_    `.
                  ___   (   `.         '\   __/   __\' /-``-.._ \
                 (   `-. `.   `.       .|\_  (   / .-| |W)|    ``'
                  `-.   `-.`.   `.     |' ( ,'\ ( (WW| \` j
          ..---'''''-`.    `.\   _\   .|   ',  \_\_`/   ``-.
        ,'            _`-,   `  (  |  |'     `.        \__/
       /   _         ( ```    __ \  \ |     ._:7,______.-'
      | .-'/          `-._   (  `.\  '':     \    /
      '`  /          .-''>`-. `-. `   |       |  (
         -          /   /    `_: `_:. `.    .  \  \
         |          |  |  o()(   (      \   )\  ;  |
        .'          `. |   Oo `---:.__-'') /  )/   |
        |            | |  ()o            |/   '    |
       .'            |/ \  o     /             \__/
       |  ,         .|   |      /-,_______\       \
      /  / )        |' _/      /     |    |\       \
    .:.-' .'         )/       /     |     | `--,    \
         /       .  / |      |      |     |   /      )
    .__.'    ,   :|/_/|      |      |      | (       |
    `-.___.-`;  / '   |      |      |      |  \      |
           .:_-'      |       \     |       \  `.___/
                       \_______)     \_______)



 */

import android.content.Context;
import android.widget.TextView;

import com.lloydtorres.stately.dto.Telegram;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.report.ReportActivity;
import com.lloydtorres.stately.wa.ResolutionActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lloyd on 2016-03-09.
 * MuffinsHelper is a collection of helper functions for processing telegrams.
 * Note that the "generics" in this function expect content from a NS telegrams page.
 */
public final class MuffinsHelper {
    public static final String TG_UNREAD = "tg_new";

    public static final String SEND_ARROW = "→";
    public static final String SEND_ARROW_SENDER_REGEX = "(?s)^(.*?)" + SEND_ARROW;
    public static final String SEND_ARROW_RECIPIENT_REGEX = "(?s)" + SEND_ARROW + "(.*?)$";
    public static final Pattern SENDER_REGEX = Pattern.compile(SEND_ARROW_SENDER_REGEX);
    public static final Pattern RECIPIENT_REGEX = Pattern.compile(SEND_ARROW_RECIPIENT_REGEX);

    public static final String NATION_FORMAT_REGEX = "@@(.*?)@@";
    public static final Pattern NATION_REGEX = Pattern.compile(NATION_FORMAT_REGEX);

    public static final String NATION_LINK_PREFIX = "nation=";
    public static final String REGION_LINK_PREFIX = "region=";
    public static final String SELF_INDICATOR = "Wired To";

    public static final String REGION_TELEGRAM = "toplinetgcat-3";
    public static final String RECRUITMENT_TELEGRAM = "toplinetgcat-1";
    public static final String MODERATOR_TELEGRAM = "toplinetgcat-11";
    public static final String WELCOME_TELEGRAM = "tag: welcome";

    // Private constructor
    private MuffinsHelper() {}

    /**
     * Takes in a JSoup Elements object containing raw telegrams from NS HTML.
     * Returns a list of telegrams obtained from the raw telegrams.
     * @param rawTelegramsContainer See above
     * @param selfName Name of current logged in nation
     * @return See above
     */
    public static ArrayList<Telegram> processRawTelegrams(Element rawTelegramsContainer, String selfName) {
        Elements rawTelegrams = rawTelegramsContainer.select("div.tg");
        ArrayList<Telegram> scannedTelegrams = new ArrayList<Telegram>();

        for (Element rt : rawTelegrams) {
            Telegram tel = new Telegram();

            // Get telegram ID
            String rtId = rt.attr("id");
            tel.id = Integer.valueOf(rtId.replace("tgid-", ""));

            // Get time of telegram
            Element timeRaw = rt.select("time").first();
            tel.timestamp = Long.valueOf(timeRaw.attr("data-epoch"));

            // Get type of telegram
            tel.type = Telegram.TELEGRAM_GENERIC;
            Element typeRaw = rt.select("div.tgtopline").first();
            if (typeRaw != null) {
                if (typeRaw.hasClass(REGION_TELEGRAM)) {
                    tel.type = Telegram.TELEGRAM_REGION;
                }
                else if (typeRaw.hasClass(RECRUITMENT_TELEGRAM)) {
                    tel.type = Telegram.TELEGRAM_RECRUITMENT;
                }
                else if (typeRaw.hasClass(MODERATOR_TELEGRAM)) {
                    tel.type = Telegram.TELEGRAM_MODERATOR;
                }
            }

            // Check if unread
            tel.isUnread = rt.classNames().contains(TG_UNREAD);

            // Get to/from fields
            Element headerRaw = rt.select("div.tg_headers").first();
            String headerRawHtml = headerRaw.html();
            if (headerRawHtml.contains(SEND_ARROW)) {
                Matcher senderMatcher = SENDER_REGEX.matcher(headerRawHtml);
                if (senderMatcher.find()) {
                    Document senderHeaderRaw = Jsoup.parse(senderMatcher.group(1), SparkleHelper.BASE_URI);
                    processSenderHeader(senderHeaderRaw, tel, selfName);
                }

                Matcher recipientsMatcher = RECIPIENT_REGEX.matcher(headerRawHtml);
                if (recipientsMatcher.find()) {
                    Document recipientsHeaderRaw = Jsoup.parse(recipientsMatcher.group(1), SparkleHelper.BASE_URI);
                    processRecipientsHeader(recipientsHeaderRaw, tel);
                }
            } else {
                Document senderHeaderRaw = Jsoup.parse(headerRawHtml);
                processSenderHeader(senderHeaderRaw, tel, selfName);
            }

            Element previewRaw = rt.select("div.tgsample").first();
            tel.preview = Jsoup.clean(previewRaw.text(), Whitelist.none().addTags("br"));
            String contentRawHtml = rt.select("div.tgmsg").first().html();
            processTelegramContent(contentRawHtml, tel);

            if (tel.type == Telegram.TELEGRAM_RECRUITMENT) {
                Element targetRegion = rt.select("input[name=region_name]").first();
                if (targetRegion != null) {
                    tel.regionTarget = targetRegion.attr("value");
                }
            }

            scannedTelegrams.add(tel);
        }

        return scannedTelegrams;
    }

    /**
     * This goes through the sender part of the raw telegram header and adds the appropriate attributes
     * to a target telegram.
     * @param targetDoc See above
     * @param targetTelegram See above
     * @param selfName Name of currently logged in nation
     */
    public static void processSenderHeader(Document targetDoc, Telegram targetTelegram, String selfName) {
        Element nationSenderRaw = targetDoc.select("a.nlink").first();
        if (nationSenderRaw != null) {
            targetTelegram.isNation = true;
            targetTelegram.sender = "@@" + SparkleHelper.getIdFromName(nationSenderRaw.attr("href").replace(NATION_LINK_PREFIX, "")) + "@@";
        } else {
            targetTelegram.isNation = false;
            targetTelegram.sender = targetDoc.text().replace("&rarr;","").trim();
            if (targetTelegram.sender.contains(SELF_INDICATOR)) {
                targetTelegram.sender = "@@" + SparkleHelper.getIdFromName(selfName) + "@@";
            }
        }
    }

    /**
     * Goes through the recipients part of the raw telegram header and builds a list of recipients.
     * @param targetDoc See above
     * @param targetTelegram See above
     */
    public static void processRecipientsHeader(Document targetDoc, Telegram targetTelegram) {
        // Check for tag:welcome here to set telegram type (since it's contained in the recepients area)
        if (targetDoc.text().contains(WELCOME_TELEGRAM)) {
            targetTelegram.type = Telegram.TELEGRAM_WELCOME;
        }

        targetTelegram.recipients = new ArrayList<String>();

        Elements nationsRaw = targetDoc.select("a.nlink");
        for (Element n : nationsRaw) {
            targetTelegram.recipients.add("@@" + SparkleHelper.getIdFromName(n.attr("href").replace(NATION_LINK_PREFIX, "")) + "@@");
        }

        Elements regionsRaw = targetDoc.select("a.rlink");
        for (Element r : regionsRaw) {
            targetTelegram.recipients.add("%%" + SparkleHelper.getIdFromName(r.attr("href").replace(REGION_LINK_PREFIX, "")) + "%%");
        }
    }

    /**
     * Takes in the raw HTML for a given telegram and processes its data.
     * @param rawHtml See above
     * @param targetTelegram Target telegram
     */
    public static void processTelegramContent(String rawHtml, Telegram targetTelegram) {
        rawHtml = "<base href=\"" + SparkleHelper.BASE_URI_NOSLASH + "\">" + rawHtml;
        Document rawContent = Jsoup.parse(rawHtml, SparkleHelper.BASE_URI);
        rawContent.select("div.tgrecruitmovebutton").remove();
        rawContent.select("p.replyline").remove();
        rawContent.select("div.inreplyto").remove();
        rawContent.select("div.rmbspacer").remove();
        targetTelegram.content = Jsoup.clean(rawContent.html(), Whitelist.basic().preserveRelativeLinks(true).addTags("br"));
    }

    public static String getNationIdFromFormat(String raw) {
        Matcher nationMatcher = NATION_REGEX.matcher(raw);
        if (nationMatcher.find()) {
            return SparkleHelper.getIdFromName(SparkleHelper.regexExtract(nationMatcher.group(0), NATION_REGEX));
        }
        return null;
    }

    public static final Pattern NS_TG_RAW_NATION_LINK = Pattern.compile("(?i)<a href=\"(?:" + SparkleHelper.BASE_URI_REGEX + "|)nation=([\\w-]*?)\" rel=\"nofollow\">(.*?)<\\/a>");
    public static final Pattern NS_TG_RAW_REGION_LINK_TG = Pattern.compile("(?i)<a href=\"(?:" + SparkleHelper.BASE_URI_REGEX + "|)region=([\\w-]*?)\\?tgid=[0-9].*\" rel=\"nofollow\">(.*?)<\\/a>");
    public static final Pattern NS_TG_RAW_REGION_LINK = Pattern.compile("(?i)<a href=\"(?:" + SparkleHelper.BASE_URI_REGEX + "|)region=([\\w-]*?)\" rel=\"nofollow\">(.*?)<\\/a>");
    public static final Pattern NS_TG_RAW_GHR_LINK = Pattern.compile("(?i)<a href=\"(?:" + SparkleHelper.BASE_URI_REGEX + "|)page=help\\?taskid=(\\d+)\" rel=\"nofollow\">");
    public static final Pattern NS_TG_RAW_RESOLUTION_LINK = Pattern.compile("(?i)<a href=\"(?:" + SparkleHelper.BASE_URI_REGEX + "|)page=WA_past_resolutions\\/council=(1|2)\\/start=([0-9]+)\" rel=\"nofollow\">");
    public static final Pattern PARAGRAPH = Pattern.compile("(?i)(?s)<p>(.*?)<\\/p>");

    /**
     * Formats raw HTML from a telegram into something the app can understand.
     * @param c App context
     * @param t TextView
     * @param content Target content
     */
    public static void setTelegramHtmlFormatting(Context c, TextView t, String content) {
        String holder = content.trim();
        holder = holder.replace("\n", "<br />");
        holder = holder.replace("&amp;#39;", "'");
        holder = holder.replace("&amp;", "&");
        holder = "<base href=\"" + SparkleHelper.BASE_URI_NOSLASH + "\">" + holder;
        holder = Jsoup.clean(holder, Whitelist.basic().preserveRelativeLinks(true).addTags("br"));
        holder = holder.replace("<a href=\"//" + SparkleHelper.DOMAIN_URI + "/", "<a href=\"" + SparkleHelper.BASE_URI);
        holder = holder.replace("<a href=\"//forum." + SparkleHelper.DOMAIN_URI + "/", "<a href=\"http://forum." + SparkleHelper.DOMAIN_URI + "/");
        holder = holder.replace("<a href=\"//www." + SparkleHelper.DOMAIN_URI + "/", "<a href=\"" + SparkleHelper.BASE_URI);
        holder = holder.replace("<a href=\"/", "<a href=\"" + SparkleHelper.BASE_URI);

        holder = SparkleHelper.regexDoubleReplace(holder, NS_TG_RAW_NATION_LINK, "<a href=\"" + ExploreActivity.EXPLORE_TARGET + "%s/" + ExploreActivity.EXPLORE_NATION + "\">%s</a>");

        holder = SparkleHelper.regexDoubleReplace(holder, NS_TG_RAW_REGION_LINK_TG, "<a href=\"" + ExploreActivity.EXPLORE_TARGET + "%s/" + ExploreActivity.EXPLORE_REGION + "\">%s</a>");
        holder = SparkleHelper.regexDoubleReplace(holder, NS_TG_RAW_REGION_LINK, "<a href=\"" + ExploreActivity.EXPLORE_TARGET + "%s/" + ExploreActivity.EXPLORE_REGION + "\">%s</a>");

        holder = SparkleHelper.regexReplace(holder, NS_TG_RAW_GHR_LINK, "<a href=\"" + ReportActivity.REPORT_TARGET + "%s\">");

        holder = SparkleHelper.regexDoubleReplace(holder, NS_TG_RAW_RESOLUTION_LINK, "<a href=\"" + ResolutionActivity.RESOLUTION_TARGET + "%s/%s\">");

        holder = SparkleHelper.regexReplace(holder, PARAGRAPH, "<br>%s");

        holder = SparkleHelper.regexGenericUrlFormat(c, holder);

        SparkleHelper.setStyledTextView(c, t, holder);
    }
}
