package com.rhcloud.igorbotian.rsskit.rest;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Igor Botian <igor.botian@gmail.com>
 */
public abstract class EntityParser<T> {

    public abstract T parse(JsonNode json) throws RestParseException;

    protected JsonNode getAttribute(JsonNode parent, String attr) throws RestParseException {
        assert parent != null;
        assert attr != null;

        if (!parent.has(attr)) {
            throw new RestParseException("Attribute is expected but not found: " + attr);
        }

        return parent.get(attr);
    }
}