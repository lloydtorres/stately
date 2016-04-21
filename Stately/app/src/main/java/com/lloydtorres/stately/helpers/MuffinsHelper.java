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

package com.lloydtorres.stately.helpers;

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

import com.lloydtorres.stately.dto.Telegram;

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
public class MuffinsHelper {
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

    /**
     * Takes in a JSoup Elements object containing raw telegrams from NS HTML.
     * Returns a list of telegrams obtained from the raw telegrams.
     * @param rawTelegramsContainer See above
     * @param selfName Name of current logged in nation
     * @param isPreview Flag for whether or not these telegrams are only meant as previews
     * @return See above
     */
    public static ArrayList<Telegram> processRawTelegrams(Element rawTelegramsContainer, String selfName, boolean isPreview)
    {
        Elements rawTelegrams = rawTelegramsContainer.select("div.tg");
        ArrayList<Telegram> scannedTelegrams = new ArrayList<Telegram>();

        for (Element rt : rawTelegrams)
        {
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
            if (typeRaw != null)
            {
                if (typeRaw.hasClass(REGION_TELEGRAM))
                {
                    tel.type = Telegram.TELEGRAM_REGION;
                }
                else if (typeRaw.hasClass(RECRUITMENT_TELEGRAM))
                {
                    tel.type = Telegram.TELEGRAM_RECRUITMENT;
                }
                else if (typeRaw.hasClass(MODERATOR_TELEGRAM))
                {
                    tel.type = Telegram.TELEGRAM_MODERATOR;
                }
            }

            // Get to/from fields
            Element headerRaw = rt.select("div.tg_headers").first();
            String headerRawHtml = headerRaw.html();
            if (headerRawHtml.contains(SEND_ARROW))
            {
                Matcher senderMatcher = SENDER_REGEX.matcher(headerRawHtml);
                if (senderMatcher.find())
                {
                    Document senderHeaderRaw = Jsoup.parse(senderMatcher.group(1), SparkleHelper.BASE_URI);
                    processSenderHeader(senderHeaderRaw, tel, selfName);
                }

                Matcher recipientsMatcher = RECIPIENT_REGEX.matcher(headerRawHtml);
                if (recipientsMatcher.find())
                {
                    Document recipientsHeaderRaw = Jsoup.parse(recipientsMatcher.group(1), SparkleHelper.BASE_URI);
                    processRecipientsHeader(recipientsHeaderRaw, tel);
                }
            }
            else
            {
                Document senderHeaderRaw = Jsoup.parse(headerRawHtml);
                processSenderHeader(senderHeaderRaw, tel, selfName);
            }

            if (isPreview)
            {
                Element previewRaw = rt.select("div.tgsample").first();
                tel.preview = Jsoup.clean(previewRaw.text(), Whitelist.none().addTags("br"));
                tel.content = null;
            }
            else
            {
                String contentRawHtml = rt.select("div.tgmsg").first().html();
                processTelegramContent(contentRawHtml, tel);
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
    public static void processSenderHeader(Document targetDoc, Telegram targetTelegram, String selfName)
    {
        Element nationSenderRaw = targetDoc.select("a.nlink").first();
        if (nationSenderRaw != null)
        {
            targetTelegram.isNation = true;
            targetTelegram.sender = "@@" + SparkleHelper.getIdFromName(nationSenderRaw.attr("href").replace(NATION_LINK_PREFIX, "")) + "@@";
        }
        else
        {
            targetTelegram.isNation = false;
            targetTelegram.sender = targetDoc.text().replace("&rarr;","").trim();
            if (targetTelegram.sender.contains(SELF_INDICATOR))
            {
                targetTelegram.sender = "@@" + SparkleHelper.getIdFromName(selfName) + "@@";
            }
        }
    }

    /**
     * Goes through the recipients part of the raw telegram header and builds a list of recipients.
     * @param targetDoc See above
     * @param targetTelegram See above
     */
    public static void processRecipientsHeader(Document targetDoc, Telegram targetTelegram)
    {
        // Check for tag:welcome here to set telegram type (since it's contained in the recepients area)
        if (targetDoc.text().contains(WELCOME_TELEGRAM))
        {
            targetTelegram.type = Telegram.TELEGRAM_WELCOME;
        }

        targetTelegram.recipients = new ArrayList<String>();

        Elements nationsRaw = targetDoc.select("a.nlink");
        for (Element n : nationsRaw)
        {
            targetTelegram.recipients.add("@@" + SparkleHelper.getIdFromName(n.attr("href").replace(NATION_LINK_PREFIX, "")) + "@@");
        }

        Elements regionsRaw = targetDoc.select("a.rlink");
        for (Element r : regionsRaw)
        {
            targetTelegram.recipients.add("%%" + SparkleHelper.getIdFromName(r.attr("href").replace(REGION_LINK_PREFIX, "")) + "%%");
        }
    }

    public static final Pattern NS_TG_RECRUIT_BUTTON = Pattern.compile("(?i)(?s)<div class=\"tgrecruitmovebutton\">(.*?)<\\/div>");
    public static final Pattern NS_TG_REPLY_LINE = Pattern.compile("(?i)(?s)<p class=\"replyline\">(.*?)<\\/p>");
    public static final Pattern NS_TG_REPLY_TO = Pattern.compile("(?i)(?s)<div class=\"inreplyto\">(.*?)<\\/div>");
    public static final Pattern NS_TG_SPACER = Pattern.compile("(?i)(?s)<div class=\"rmbspacer\">(.*?)<\\/div>");

    /**
     * Takes in the raw HTML for a given telegram and processes its data.
     * @param rawHtml See above
     * @param targetTelegram Target telegram
     */
    public static void processTelegramContent(String rawHtml, Telegram targetTelegram)
    {
        rawHtml = "<base href=\"" + SparkleHelper.BASE_URI_NOSLASH + "\">" + rawHtml;
        rawHtml = SparkleHelper.regexRemove(rawHtml, NS_TG_RECRUIT_BUTTON);
        rawHtml = SparkleHelper.regexRemove(rawHtml, NS_TG_REPLY_LINE);
        rawHtml = SparkleHelper.regexRemove(rawHtml, NS_TG_REPLY_TO);
        rawHtml = SparkleHelper.regexRemove(rawHtml, NS_TG_SPACER);
        targetTelegram.content = Jsoup.clean(rawHtml, Whitelist.basic().preserveRelativeLinks(true).addTags("br"));
    }

    public static String getNationIdFromFormat(String raw)
    {
        Matcher nationMatcher = NATION_REGEX.matcher(raw);
        if (nationMatcher.find())
        {
            return SparkleHelper.getIdFromName(SparkleHelper.regexExtract(nationMatcher.group(0), NATION_REGEX));
        }
        return null;
    }
}
