/*
 * Copyright (C) Sportradar AG. See LICENSE for full license governing this code
 */

package com.sportradar.mts.sdk.api.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sportradar.mts.sdk.api.Bet;
import com.sportradar.mts.sdk.api.Selection;
import com.sportradar.mts.sdk.api.Sender;
import com.sportradar.mts.sdk.api.Ticket;
import com.sportradar.mts.sdk.api.enums.OddsChangeType;
import com.sportradar.mts.sdk.api.exceptions.MtsSdkProcessException;
import com.sportradar.mts.sdk.api.utils.JsonUtils;
import com.sportradar.mts.sdk.api.utils.MtsDtoMapper;
import com.sportradar.mts.sdk.api.utils.MtsTicketHelper;
import com.sportradar.mts.sdk.api.utils.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of Ticket
 */
public class TicketImpl implements Ticket {

    private final String ticketId;
    private final Date timestampUtc;
    private final String version;
    private final List<Bet> bets;
    private final Sender sender;
    private final String reofferId;
    private final String altStakeRefId;
    private final boolean testSource;
    private final OddsChangeType oddsChangeType;
    private final List<Selection> selections;
    private final String correlationId;
    private final Integer totalCombinations;
    private final Date lastMatchEndTime;
    private final Long payCap;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public TicketImpl(@JsonProperty("ticketId") String ticketId,
                      @JsonProperty("bets") List<Bet> bets,
                      @JsonProperty("sender") Sender sender,
                      @JsonProperty("reofferId") String reofferId,
                      @JsonProperty("altStakeRefId") String altStakeRefId,
                      @JsonProperty("testSource") boolean isTestSource,
                      @JsonProperty("oddsChange") OddsChangeType oddsChangeType,
                      @JsonProperty("totalCombinations") Integer totalCombinations,
                      @JsonProperty("lastMatchEndTime") Date lastMatchEndTime,
                      @JsonProperty("payCap") Long payCap,
                      @JsonProperty("timestampUtc") Date timestampUtc,
                      @JsonProperty("version") String version) {
        Preconditions.checkNotNull(ticketId, "ticketId cannot be null");
        Preconditions.checkArgument(MtsTicketHelper.validateTicketId(ticketId), "ticketId is not valid");
        Preconditions.checkNotNull(bets, "bet cannot be null");
        Preconditions.checkArgument(!bets.isEmpty(), "need at least one bet");
        Preconditions.checkArgument(bets.size() <= 50, "maximum 50 bets per ticket");
        Preconditions.checkNotNull(sender, "sender is missing");
        Preconditions.checkArgument(reofferId == null || (reofferId.length() > 0 && reofferId.length() <= 128), "reofferId is not valid");
        Preconditions.checkArgument(altStakeRefId == null || (altStakeRefId.length() > 0 && altStakeRefId.length() <= 128), "altStakeRefId is not valid");
        Preconditions.checkNotNull(timestampUtc, "timestamp cannot be null");
        Preconditions.checkArgument(MtsTicketHelper.validateTimestamp(timestampUtc), "timestampUtc is not valid");
        Preconditions.checkNotNull(version, "version cannot be null");
        Preconditions.checkArgument(version.length() == 3, "version is not valid");

        this.ticketId = ticketId;
        this.bets = bets;
        this.sender = sender;
        this.reofferId = reofferId;
        this.altStakeRefId = altStakeRefId;
        this.testSource = isTestSource;
        this.oddsChangeType = oddsChangeType;
        this.timestampUtc = timestampUtc;
        this.version = version;
        this.lastMatchEndTime = lastMatchEndTime;
        this.payCap = payCap;

        if (!StringUtils.isNullOrEmpty(reofferId) && !StringUtils.isNullOrEmpty(altStakeRefId))
        {
            throw new IllegalArgumentException("Only ReofferId or AltStakeRefId can specified.");
        }

        List<Selection> selectionList = Lists.newArrayList();
        for (Bet bet : bets)
        {
            selectionList.addAll(bet.getSelections());
        }
        this.selections = selectionList.stream().distinct().collect(Collectors.toList());
        this.correlationId = MtsTicketHelper.generateTicketCorrelationId();
        this.totalCombinations = totalCombinations;
    }

    @Override
    public String getTicketId() {
        return ticketId;
    }

    @Override
    public Date getTimestampUtc() {
        return timestampUtc;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public List<Bet> getBets() {
        return bets;
    }

    @Override
    public Sender getSender() {
        return sender;
    }

    @Override
    public String getReofferId() {
        return reofferId;
    }

    @Override
    public String getAltStakeRefId() {
        return altStakeRefId;
    }

    @Override
    public boolean getTestSource() {
        return testSource;
    }

    @Override
    public OddsChangeType getOddsChange() {
        return oddsChangeType;
    }

    @Override
    public List<Selection> getSelections() { return selections; }

    @Override
    public Integer getTotalCombinations() {
        return totalCombinations;
    }

    @Override
    public Date getLastMatchEndTime() {
        return lastMatchEndTime;
    }

    @Override
    public Long getPayCap() {
        return payCap;
    }

    /**
     * Returns the correlation id
     * @return correlation id
     */
    @Override
    public String getCorrelationId() { return correlationId; }

    /**
     * Returns the associated ticket in the required MTS JSON format
     *
     * @return the associated ticket in the required MTS JSON format
     */
    @Override
    @JsonIgnore
    public String getJsonValue() {
        com.sportradar.mts.sdk.api.impl.mtsdto.ticket.TicketSchema dto = MtsDtoMapper.map(this);
        try {
            return JsonUtils.OBJECT_MAPPER.writeValueAsString(dto.getTicket());
        }
        catch (JsonProcessingException ex)
        {
            throw new MtsSdkProcessException("Exception during dto mapping: " + ex.getMessage(), ex.getCause());
        }
    }

    @Override
    public String toString() {
        return "TicketImpl{" +
                "bets=" + bets.size() +
                ", ticketId='" + ticketId + '\'' +
                ", reofferId=" + reofferId +
                ", altStakeRefId=" + altStakeRefId +
                ", sender=" + sender +
                ", timestamp=" + timestampUtc +
                ", version='" + version + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", totalCombinations=" + totalCombinations +
                ", lastMatchEndTime=" + lastMatchEndTime +
                '}';
    }
}
