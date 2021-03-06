package com.cloudbees.demo.gasp.model;

/*
 * Copyright (c) 2013 Mark Prichard, CloudBees
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * User: Mark Prichard (mprichard@cloudbees.com)
 * Date: 8/27/13
 */
public class Location implements Serializable {
    private BigDecimal lat;
    private BigDecimal lng;

    public BigDecimal getLat() {
        return lat;
    }

    public BigDecimal getLng() {
        return lng;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public void setLng(BigDecimal lng) {
        this.lng = lng;
    }
}