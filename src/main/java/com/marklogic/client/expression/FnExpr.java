/*
 * Copyright 2016 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.client.expression;

import com.marklogic.client.type.ElementNodeExpr;
 import com.marklogic.client.type.ItemExpr;
 import com.marklogic.client.type.ItemSeqExpr;
 import com.marklogic.client.type.NodeExpr;
 import com.marklogic.client.type.XsAnyAtomicTypeExpr;
 import com.marklogic.client.type.XsAnyAtomicTypeSeqExpr;
 import com.marklogic.client.type.XsAnyURIExpr;
 import com.marklogic.client.type.XsBooleanExpr;
 import com.marklogic.client.type.XsDateExpr;
 import com.marklogic.client.type.XsDateTimeExpr;
 import com.marklogic.client.type.XsDayTimeDurationExpr;
 import com.marklogic.client.type.XsDecimalExpr;
 import com.marklogic.client.type.XsDoubleExpr;
 import com.marklogic.client.type.XsDurationExpr;
 import com.marklogic.client.type.XsIntegerExpr;
 import com.marklogic.client.type.XsIntegerSeqExpr;
 import com.marklogic.client.type.XsNCNameExpr;
 import com.marklogic.client.type.XsNumericExpr;
 import com.marklogic.client.type.XsNumericSeqExpr;
 import com.marklogic.client.type.XsQNameExpr;
 import com.marklogic.client.type.XsStringExpr;
 import com.marklogic.client.type.XsStringSeqExpr;
 import com.marklogic.client.type.XsTimeExpr;


// IMPORTANT: Do not edit. This file is generated. 
public interface FnExpr {
    public XsNumericExpr abs(XsNumericExpr arg);
    public XsDateExpr adjustDateToTimezone(XsDateExpr arg);
    public XsDateExpr adjustDateToTimezone(XsDateExpr arg, XsDayTimeDurationExpr timezone);
    public XsDateTimeExpr adjustDateTimeToTimezone(XsDateTimeExpr arg);
    public XsDateTimeExpr adjustDateTimeToTimezone(XsDateTimeExpr arg, XsDayTimeDurationExpr timezone);
    public XsTimeExpr adjustTimeToTimezone(XsTimeExpr arg);
    public XsTimeExpr adjustTimeToTimezone(XsTimeExpr arg, XsDayTimeDurationExpr timezone);
    public ElementNodeExpr analyzeString(XsStringExpr in, String regex);
    public ElementNodeExpr analyzeString(XsStringExpr in, XsStringExpr regex);
    public ElementNodeExpr analyzeString(XsStringExpr in, String regex, String flags);
    public ElementNodeExpr analyzeString(XsStringExpr in, XsStringExpr regex, XsStringExpr flags);
    public XsAnyAtomicTypeExpr avg(XsAnyAtomicTypeSeqExpr arg);
    public XsAnyURIExpr baseUri(NodeExpr arg);
    public XsBooleanExpr booleanExpr(ItemSeqExpr arg);
    public XsNumericExpr ceiling(XsNumericExpr arg);
    public XsBooleanExpr codepointEqual(XsStringExpr comparand1, String comparand2);
    public XsBooleanExpr codepointEqual(XsStringExpr comparand1, XsStringExpr comparand2);
    public XsStringExpr codepointsToString(XsIntegerSeqExpr arg);
    public XsIntegerExpr compare(XsStringExpr comparand1, String comparand2);
    public XsIntegerExpr compare(XsStringExpr comparand1, XsStringExpr comparand2);
    public XsIntegerExpr compare(XsStringExpr comparand1, String comparand2, String collation);
    public XsIntegerExpr compare(XsStringExpr comparand1, XsStringExpr comparand2, XsStringExpr collation);
    public XsStringExpr concat(XsAnyAtomicTypeExpr... parameter1);
    public XsIntegerExpr count(ItemSeqExpr arg);
    public XsIntegerExpr count(ItemSeqExpr arg, double maximum);
    public XsIntegerExpr count(ItemSeqExpr arg, XsDoubleExpr maximum);
    public XsDateExpr currentDate();
    public XsDateTimeExpr currentDateTime();
    public XsTimeExpr currentTime();
    public XsBooleanExpr contains(XsStringExpr parameter1, String parameter2);
    public XsBooleanExpr contains(XsStringExpr parameter1, XsStringExpr parameter2);
    public XsBooleanExpr contains(XsStringExpr parameter1, String parameter2, String collation);
    public XsBooleanExpr contains(XsStringExpr parameter1, XsStringExpr parameter2, XsStringExpr collation);
    public XsIntegerExpr dayFromDate(XsDateExpr arg);
    public XsIntegerExpr dayFromDateTime(XsDateTimeExpr arg);
    public XsIntegerExpr daysFromDuration(XsDurationExpr arg);
    public XsBooleanExpr deepEqual(ItemSeqExpr parameter1, ItemSeqExpr parameter2);
    public XsBooleanExpr deepEqual(ItemSeqExpr parameter1, ItemSeqExpr parameter2, String collation);
    public XsBooleanExpr deepEqual(ItemSeqExpr parameter1, ItemSeqExpr parameter2, XsStringExpr collation);
    public XsStringExpr defaultCollation();
    public XsAnyAtomicTypeSeqExpr distinctValues(XsAnyAtomicTypeSeqExpr arg);
    public XsAnyAtomicTypeSeqExpr distinctValues(XsAnyAtomicTypeSeqExpr arg, String collation);
    public XsAnyAtomicTypeSeqExpr distinctValues(XsAnyAtomicTypeSeqExpr arg, XsStringExpr collation);
    public XsAnyURIExpr documentUri(NodeExpr arg);
    public XsBooleanExpr empty(ItemSeqExpr arg);
    public XsStringExpr encodeForUri(XsStringExpr uriPart);
    public XsBooleanExpr endsWith(XsStringExpr parameter1, String parameter2);
    public XsBooleanExpr endsWith(XsStringExpr parameter1, XsStringExpr parameter2);
    public XsBooleanExpr endsWith(XsStringExpr parameter1, String parameter2, String collation);
    public XsBooleanExpr endsWith(XsStringExpr parameter1, XsStringExpr parameter2, XsStringExpr collation);
    public XsStringExpr escapeHtmlUri(XsStringExpr uriPart);
    public XsBooleanExpr exists(ItemSeqExpr arg);
    public XsBooleanExpr falseExpr();
    public XsNumericExpr floor(XsNumericExpr arg);
    public XsStringExpr formatDate(XsDateExpr value, String picture);
    public XsStringExpr formatDate(XsDateExpr value, XsStringExpr picture);
    public XsStringExpr formatDateTime(XsDateTimeExpr value, String picture);
    public XsStringExpr formatDateTime(XsDateTimeExpr value, XsStringExpr picture);
    public XsStringExpr formatNumber(XsNumericSeqExpr value, String picture);
    public XsStringExpr formatNumber(XsNumericSeqExpr value, XsStringExpr picture);
    public XsStringExpr formatNumber(XsNumericSeqExpr value, String picture, String decimalFormatName);
    public XsStringExpr formatNumber(XsNumericSeqExpr value, XsStringExpr picture, XsStringExpr decimalFormatName);
    public XsStringExpr formatTime(XsTimeExpr value, String picture);
    public XsStringExpr formatTime(XsTimeExpr value, XsStringExpr picture);
    public XsStringExpr generateId(NodeExpr node);
    public ItemExpr head(ItemSeqExpr arg1);
    public XsIntegerExpr hoursFromDateTime(XsDateTimeExpr arg);
    public XsIntegerExpr hoursFromDuration(XsDurationExpr arg);
    public XsIntegerExpr hoursFromTime(XsTimeExpr arg);
    public XsDayTimeDurationExpr implicitTimezone();
    public XsIntegerSeqExpr indexOf(XsAnyAtomicTypeSeqExpr seqParam, XsAnyAtomicTypeExpr srchParam);
    public XsIntegerSeqExpr indexOf(XsAnyAtomicTypeSeqExpr seqParam, XsAnyAtomicTypeExpr srchParam, String collationLiteral);
    public XsIntegerSeqExpr indexOf(XsAnyAtomicTypeSeqExpr seqParam, XsAnyAtomicTypeExpr srchParam, XsStringExpr collationLiteral);
    public XsStringSeqExpr inScopePrefixes(ElementNodeExpr element);
    public ItemSeqExpr insertBefore(ItemSeqExpr target, XsIntegerExpr position, ItemSeqExpr inserts);
    public XsStringExpr iriToUri(XsStringExpr uriPart);
    public XsBooleanExpr lang(XsStringExpr testlang, NodeExpr node);
    public XsStringExpr localName(NodeExpr arg);
    public XsNCNameExpr localNameFromQName(XsQNameExpr arg);
    public XsStringExpr lowerCase(XsStringExpr string);
    public XsBooleanExpr matches(XsStringExpr input, String pattern);
    public XsBooleanExpr matches(XsStringExpr input, XsStringExpr pattern);
    public XsBooleanExpr matches(XsStringExpr input, String pattern, String flags);
    public XsBooleanExpr matches(XsStringExpr input, XsStringExpr pattern, XsStringExpr flags);
    public XsAnyAtomicTypeExpr max(XsAnyAtomicTypeSeqExpr arg);
    public XsAnyAtomicTypeExpr max(XsAnyAtomicTypeSeqExpr arg, String collation);
    public XsAnyAtomicTypeExpr max(XsAnyAtomicTypeSeqExpr arg, XsStringExpr collation);
    public XsAnyAtomicTypeExpr min(XsAnyAtomicTypeSeqExpr arg);
    public XsAnyAtomicTypeExpr min(XsAnyAtomicTypeSeqExpr arg, String collation);
    public XsAnyAtomicTypeExpr min(XsAnyAtomicTypeSeqExpr arg, XsStringExpr collation);
    public XsIntegerExpr minutesFromDateTime(XsDateTimeExpr arg);
    public XsIntegerExpr minutesFromDuration(XsDurationExpr arg);
    public XsIntegerExpr minutesFromTime(XsTimeExpr arg);
    public XsIntegerExpr monthFromDate(XsDateExpr arg);
    public XsIntegerExpr monthFromDateTime(XsDateTimeExpr arg);
    public XsIntegerExpr monthsFromDuration(XsDurationExpr arg);
    public XsStringExpr name(NodeExpr arg);
    public XsAnyURIExpr namespaceUri(NodeExpr arg);
    public XsAnyURIExpr namespaceUriForPrefix(XsStringExpr prefix, ElementNodeExpr element);
    public XsAnyURIExpr namespaceUriFromQName(XsQNameExpr arg);
    public XsBooleanExpr nilled();
    public XsBooleanExpr nilled(NodeExpr arg);
    public XsQNameExpr nodeName();
    public XsQNameExpr nodeName(NodeExpr arg);
    public XsStringExpr normalizeSpace(XsStringExpr input);
    public XsStringExpr normalizeUnicode(XsStringExpr arg);
    public XsStringExpr normalizeUnicode(XsStringExpr arg, String normalizationForm);
    public XsStringExpr normalizeUnicode(XsStringExpr arg, XsStringExpr normalizationForm);
    public XsBooleanExpr not(ItemSeqExpr arg);
    public XsDoubleExpr number(XsAnyAtomicTypeExpr arg);
    public XsNCNameExpr prefixFromQName(XsQNameExpr arg);
    public XsQNameExpr QName(XsStringExpr paramURI, String paramQName);
    public XsQNameExpr QName(XsStringExpr paramURI, XsStringExpr paramQName);
    public ItemSeqExpr remove(ItemSeqExpr target, XsIntegerExpr position);
    public XsStringExpr replace(XsStringExpr input, String pattern, String replacement);
    public XsStringExpr replace(XsStringExpr input, XsStringExpr pattern, XsStringExpr replacement);
    public XsStringExpr replace(XsStringExpr input, String pattern, String replacement, String flags);
    public XsStringExpr replace(XsStringExpr input, XsStringExpr pattern, XsStringExpr replacement, XsStringExpr flags);
    public XsQNameExpr resolveQName(XsStringExpr qname, ElementNodeExpr element);
    public XsAnyURIExpr resolveUri(XsStringExpr relative, String base);
    public XsAnyURIExpr resolveUri(XsStringExpr relative, XsStringExpr base);
    public ItemSeqExpr reverse(ItemSeqExpr target);
    public NodeExpr root(NodeExpr arg);
    public XsNumericExpr round(XsNumericExpr arg);
    public XsNumericExpr roundHalfToEven(XsNumericExpr arg);
    public XsNumericExpr roundHalfToEven(XsNumericExpr arg, XsIntegerExpr precision);
    public XsDecimalExpr secondsFromDateTime(XsDateTimeExpr arg);
    public XsDecimalExpr secondsFromDuration(XsDurationExpr arg);
    public XsDecimalExpr secondsFromTime(XsTimeExpr arg);
    public XsBooleanExpr startsWith(XsStringExpr parameter1, String parameter2);
    public XsBooleanExpr startsWith(XsStringExpr parameter1, XsStringExpr parameter2);
    public XsBooleanExpr startsWith(XsStringExpr parameter1, String parameter2, String collation);
    public XsBooleanExpr startsWith(XsStringExpr parameter1, XsStringExpr parameter2, XsStringExpr collation);
    public XsStringExpr string(ItemExpr arg);
    public XsStringExpr stringJoin(XsStringSeqExpr parameter1);
    public XsStringExpr stringJoin(XsStringSeqExpr parameter1, String parameter2);
    public XsStringExpr stringJoin(XsStringSeqExpr parameter1, XsStringExpr parameter2);
    public XsIntegerExpr stringLength(XsStringExpr sourceString);
    public XsIntegerSeqExpr stringToCodepoints(XsStringExpr arg);
    public ItemSeqExpr subsequence(ItemSeqExpr sourceSeq, XsNumericExpr startingLoc);
    public ItemSeqExpr subsequence(ItemSeqExpr sourceSeq, XsNumericExpr startingLoc, XsNumericExpr length);
    public XsStringExpr substring(XsStringExpr sourceString, XsNumericExpr startingLoc);
    public XsStringExpr substring(XsStringExpr sourceString, XsNumericExpr startingLoc, XsNumericExpr length);
    public XsStringExpr substringAfter(XsStringExpr input, String after);
    public XsStringExpr substringAfter(XsStringExpr input, XsStringExpr after);
    public XsStringExpr substringAfter(XsStringExpr input, String after, String collation);
    public XsStringExpr substringAfter(XsStringExpr input, XsStringExpr after, XsStringExpr collation);
    public XsStringExpr substringBefore(XsStringExpr input, String before);
    public XsStringExpr substringBefore(XsStringExpr input, XsStringExpr before);
    public XsStringExpr substringBefore(XsStringExpr input, String before, String collation);
    public XsStringExpr substringBefore(XsStringExpr input, XsStringExpr before, XsStringExpr collation);
    public XsAnyAtomicTypeExpr sum(XsAnyAtomicTypeSeqExpr arg);
    public XsAnyAtomicTypeExpr sum(XsAnyAtomicTypeSeqExpr arg, XsAnyAtomicTypeExpr zero);
    public ItemSeqExpr tail(ItemSeqExpr seq);
    public XsDayTimeDurationExpr timezoneFromDate(XsDateExpr arg);
    public XsDayTimeDurationExpr timezoneFromDateTime(XsDateTimeExpr arg);
    public XsDayTimeDurationExpr timezoneFromTime(XsTimeExpr arg);
    public XsStringSeqExpr tokenize(XsStringExpr input, String pattern);
    public XsStringSeqExpr tokenize(XsStringExpr input, XsStringExpr pattern);
    public XsStringSeqExpr tokenize(XsStringExpr input, String pattern, String flags);
    public XsStringSeqExpr tokenize(XsStringExpr input, XsStringExpr pattern, XsStringExpr flags);
    public XsStringExpr translate(XsStringExpr src, String mapString, String transString);
    public XsStringExpr translate(XsStringExpr src, XsStringExpr mapString, XsStringExpr transString);
    public XsBooleanExpr trueExpr();
    public ItemSeqExpr unordered(ItemSeqExpr sourceSeq);
    public XsStringExpr upperCase(XsStringExpr string);
    public XsIntegerExpr yearFromDate(XsDateExpr arg);
    public XsIntegerExpr yearFromDateTime(XsDateTimeExpr arg);
    public XsIntegerExpr yearsFromDuration(XsDurationExpr arg);
}