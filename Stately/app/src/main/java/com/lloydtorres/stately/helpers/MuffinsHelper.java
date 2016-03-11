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
    public static final String SEND_ARROW_RECEPIENT_REGEX = "(?s)" + SEND_ARROW + "(.*?)$";
    public static final Pattern SENDER_REGEX = Pattern.compile(SEND_ARROW_SENDER_REGEX);
    public static final Pattern RECEPIENT_REGEX = Pattern.compile(SEND_ARROW_RECEPIENT_REGEX);

    public static final String NATION_LINK_PREFIX = "nation=";
    public static final String REGION_LINK_PREFIX = "region=";
    public static final String SELF_INDICATOR = "Wired To";

    public static final String REGION_TELEGRAM = "toplinetgcat-3";
    public static final String RECRUITMENT_TELEGRAM = "toplinetgcat-1";
    public static final String MODERATOR_TELEGRAM = "toplinetgcat-11";

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

                Matcher recepientsMatcher = RECEPIENT_REGEX.matcher(headerRawHtml);
                if (recepientsMatcher.find())
                {
                    Document recepientsHeaderRaw = Jsoup.parse(recepientsMatcher.group(1), SparkleHelper.BASE_URI);
                    processRecepientsHeader(recepientsHeaderRaw, tel);
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
     * Goes through the recepients part of the raw telegram header and builds a list of recepients.
     * @param targetDoc See above
     * @param targetTelegram See above
     */
    public static void processRecepientsHeader(Document targetDoc, Telegram targetTelegram)
    {
        targetTelegram.recepients = new ArrayList<String>();

        Elements nationsRaw = targetDoc.select("a.nlink");
        for (Element n : nationsRaw)
        {
            targetTelegram.recepients.add("@@" + SparkleHelper.getIdFromName(n.attr("href").replace(NATION_LINK_PREFIX, "")) + "@@");
        }

        Elements regionsRaw = targetDoc.select("a.rlink");
        for (Element r : regionsRaw)
        {
            targetTelegram.recepients.add("%%" + SparkleHelper.getIdFromName(r.attr("href").replace(REGION_LINK_PREFIX, "")) + "%%");
        }
    }

    /**
     * Takes in the raw HTML for a given telegram and processes its data.
     * @param rawHtml See above
     * @param targetTelegram Target telegram
     */
    public static void processTelegramContent(String rawHtml, Telegram targetTelegram)
    {
        rawHtml = SparkleHelper.regexRemove(rawHtml, "(?s)<div class=\"tgrecruitmovebutton\">.*?<\\/div>");
        rawHtml = SparkleHelper.regexRemove(rawHtml, "(?s)<p class=\"replyline\">(.*?)<\\/p>");
        targetTelegram.content = Jsoup.clean(rawHtml, Whitelist.basic().addTags("br"));
    }
}
