/*
 * Video Telemetry for Mountain Bike Platform back-end services.
 * Copyright (C) 2017 Kyle Grund
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vitembp.embedded.data;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.apache.logging.log4j.LogManager;

/**
 * Provides paging functionality for Samples.
 */
class SamplePage {
    /**
     * The XML tag for serializing a sample page.
     */
    private static final String SAMPLE_PAGE_TAG = "samplepage";
    
    /**
     * The XML tag for serializing the UUID for the next sample page.
     */
    private static final String NEXT_TAG = "next";
    
    /**
     * The XML tag for serializing the sample page start index.
     */
    private static final String START_INDEX_TAG = "startindex";
    
    /**
     * The XML tag for serializing the sample page size.
     */
    private static final String PAGE_SIZE_TAG = "pagesize";
    
    /**
     * The XML tag for serializing the sample page sample start time.
     */
    private static final String START_TIME_TAG = "starttime";
    
    /**
     * The XML tag for serializing the sample page sample interval.
     */
    private static final String SAMPLE_INTERVAL_TAG = "sampleinterval";
    
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * The starting index of samples in this page.
     */
    private int startIndex;
    
    /**
     * The number of samples this page should hold.
     */
    private int pageSize;
    
    /**
     * The samples in this page.
     */
    private final List<Sample> samples;
    
    /**
     * The UUID of the next page.
     */
    private UUID nextPage;
    
    /**
     * The time between samples in nanoseconds.
     */
    private long nanosecondInterval;
    
    /**
     * The time of the first sample.
     */
    private Instant startTime;
    
    /**
     * The interface to read and write into the backing store.
     */
    private final UuidStringLocation store;
    
    /**
     * Initializes a new instance of the SamplePage class.
     * @param store The backing store to save and load pages from.
     * @param location The location of this page in the backing store.
     * @param startIndex The starting index of samples in this page.
     * @param pageSize The number of samples this page should hold.
     */
    public SamplePage(UuidStringLocation store, int startIndex, int pageSize, Instant startTime, long nanosecondInterval) {
        // save parameters
        this.store = store;
        this.startIndex = startIndex;
        this.pageSize = pageSize;
        this.nanosecondInterval = nanosecondInterval;
        this.startTime = startTime;
        this.nextPage = UUID.randomUUID();
        
        // initialize local sample store
        this.samples = new ArrayList<>();
    }
    
    /**
     * Gets a sample by index.
     * @param index The index of the sample to get.
     */
    public Sample getSample(int index) throws SamplePageFaultException {
        // check sample bounds
        if (!this.containsSample(index)) {
            StringBuilder message = new StringBuilder();
            message.append("Page ");
            message.append(startIndex);
            message.append(" does not contain index ");
            message.append(index);
            message.append(".");
            throw new SamplePageFaultException(message.toString());
        }
        
        return this.samples.get(index - this.startIndex);
    }
    
    /**
     * Adds a sample to the page.
     * @param toAdd The sample to add.
     * @throws SamplePageFaultException If the sample cannot be added because
     * the page is full or the sample index is incorrect.
     */
    public void addSample(Sample toAdd) throws SamplePageFaultException {
        // check that the page is not full
        if (this.isFull()) {
            throw new SamplePageFaultException("Page full, cannot add sample.");
        }
        
        // check sample index
        int nextPageIndex = this.startIndex + this.samples.size();
        if (toAdd.getIndex() != nextPageIndex) {
            StringBuilder message = new StringBuilder();
            message.append("Sample index, ");
            message.append(toAdd.getIndex());
            message.append(", does not match next page index, ");
            message.append(nextPageIndex);
            message.append(".");
            throw new SamplePageFaultException(message.toString());
        }
        
        this.samples.add(toAdd);
    }
    
    /**
     * Returns a boolean value indicating whether the sample specified by the
     * index is contained in this page.
     * @param index The index of the sample to check for.
     * @return A boolean value indicating whether the sample specified by the
     * index is contained in this page.
     */
    public boolean containsSample(int index) {
        // calculate the offset of the requested sample in the local data store
        int indexInPage = index - this.startIndex;
        
        // check index is in bounds
        return (indexInPage >= 0 && indexInPage < this.samples.size());
    }
    
    /**
     * Returns a value indicating whether more samples can be added to this page.
     * @return A value indicating whether more samples can be added to this page.
     */
    public boolean isFull() {
        return this.samples.size() < this.pageSize;
    }
    
    /**
     * Gets the number of samples currently loaded in this page.
     * @return The number of samples currently loaded in this page.
     */
    public int sampleCount() {
        return this.samples.size();
    }
    
    /**
     * Returns a new sample page for the next page location. If this is the last
     * page null is returned.
     * @return A new sample page for the next page location.
     */
    SamplePage getNextPage() {
        // if there is no next page
        if (this.samples.size() < this.pageSize) {
            return null;
        }
        
        // return a new, and loaded, page object refrencing the new page
        SamplePage toRet = new SamplePage(
                this.store.getNewLocation(this.nextPage),
                this.startIndex + this.pageSize,
                this.pageSize,
                this.startTime,
                this.nanosecondInterval);
        
        // try to load previously saved data
        try {
            toRet.load();
        } catch (XMLStreamException ex) {
            LOGGER.error("Could not load the next sample page.", ex);
        }
        
        return toRet;
    }
    
    /**
     * Saves this page to the underlying data store.
     * @throws XMLStreamException If an exception occurs serializing this page.
     */
    void save() throws XMLStreamException {
        StringWriter sw = new StringWriter();
        XMLStreamWriter toWriteTo = XMLOutputFactory.newFactory().createXMLStreamWriter(sw);
        this.writeTo(toWriteTo);
        try {
            this.store.write(sw.toString());
        } catch (IOException ex) {
            throw new XMLStreamException("IO Exception occured writing SamplePage from persistant storage.", ex);
        }
    }
    
    /**
     * Loads this page from the underlying data store.
     * @throws XMLStreamException If an exception occurs de-serializing this page.
     */
    void load() throws XMLStreamException {
        // try to read in any previously saved data
        String savedData = "";
        try {
            savedData = this.store.read();
        } catch (IOException ex) {
            throw new XMLStreamException("IO Exception reading SamplePage from persistnat storage.", ex);
        }
        
        // we will only get data if a page has been previously saved, if there
        // is none we can just go with defaults as this is a new page
        if (!"".equals(savedData)) {
            StringReader sr = new StringReader(savedData);
            XMLStreamReader toReadFrom = XMLInputFactory.newFactory().createXMLStreamReader(sr);
            this.readFrom(toReadFrom);
        }
    }
    
    /**
     * Writes this sample to an XMLStreamWriter.
     * @param toWriteTo The XMLStreamWriter to write to.
     * @throws XMLStreamException If an exception occurs writing to the stream.
     */
    private void writeTo(XMLStreamWriter toWriteTo) throws XMLStreamException {
        toWriteTo.writeStartElement(SAMPLE_PAGE_TAG);
        
        // save the index of the first sample
        toWriteTo.writeStartElement(START_INDEX_TAG);
        toWriteTo.writeCharacters(Integer.toString(this.startIndex));
        toWriteTo.writeEndElement();
        
        // save the size of the page
        toWriteTo.writeStartElement(PAGE_SIZE_TAG);
        toWriteTo.writeCharacters(Integer.toString(this.pageSize));
        toWriteTo.writeEndElement();
        
        // save the time of the first element in any page
        toWriteTo.writeStartElement(START_TIME_TAG);
        toWriteTo.writeCharacters(this.startTime.toString());
        toWriteTo.writeEndElement();
        
        // save the time interval between each element
        toWriteTo.writeStartElement(SAMPLE_INTERVAL_TAG);
        toWriteTo.writeCharacters(Long.toString(this.nanosecondInterval));
        toWriteTo.writeEndElement();
        
        // save the UUID for the next page
        toWriteTo.writeStartElement(NEXT_TAG);
        toWriteTo.writeCharacters(this.nextPage.toString());
        toWriteTo.writeEndElement();
        
        // save the UUID for the next page
        toWriteTo.writeStartElement(NEXT_TAG);
        toWriteTo.writeCharacters(this.nextPage.toString());
        toWriteTo.writeEndElement();
        
        // save all sample data
        toWriteTo.writeStartElement("samples");
        for (Sample toWrite : this.samples) {
            toWrite.writeTo(toWriteTo);
        }
        toWriteTo.writeEndElement();
        toWriteTo.writeEndElement();
        toWriteTo.writeEndDocument();
    }
    
    /**
     * Read data for this Capture from an XMLStreamWriter.
     * @param toReadFrom The XMLStreamReader to read from.
     * @throws XMLStreamException If an exception occurs writing to the stream.
     */
    private void readFrom(XMLStreamReader toReadFrom) throws XMLStreamException {
        // read into sample page element
        if (toReadFrom.next() != XMLStreamConstants.START_ELEMENT || !SAMPLE_PAGE_TAG.equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <samplepage> not found.", toReadFrom.getLocation());
        }
        
        // read starting index        
        int startSampleIndex = Integer.parseInt(this.readElement(START_INDEX_TAG, toReadFrom));
        
        // read page size
        int maxPageSize = Integer.parseInt(this.readElement(PAGE_SIZE_TAG, toReadFrom));
        
        // read starting index        
        Instant sampleStartTime = Instant.parse(this.readElement(START_TIME_TAG, toReadFrom));
        
        // read page size
        long sampleInterval = Integer.parseInt(this.readElement(SAMPLE_INTERVAL_TAG, toReadFrom));
        
        // read next UUID
        UUID nextPageUuid = UUID.fromString(this.readElement(NEXT_TAG, toReadFrom));
        
        // keeps track of the index of the sample that is currently being loaded
        int sampleIndexAt =  this.startIndex;
        
        // read into samples element
        if (toReadFrom.next() != XMLStreamConstants.START_ELEMENT || !"samples".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <samples> not found.", toReadFrom.getLocation());
        }
        
        // holds samples that are loaded
        List<Sample> readSamples = new ArrayList<>();
        
        // add a sensor element for each data entry
        while (toReadFrom.next() == XMLStreamConstants.START_ELEMENT && "sample".equals(toReadFrom.getLocalName())) {
            readSamples.add(new Sample(
                    sampleIndexAt,
                    sampleStartTime.plusNanos(sampleInterval * sampleIndexAt),
                    toReadFrom));
        }
        
        // read into close element
        if (toReadFrom.getEventType() != XMLStreamConstants.END_ELEMENT || !"sensors".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </sensors> not found.", toReadFrom.getLocation());
        }
        
        // read into samples element
        if (toReadFrom.next() != XMLStreamConstants.START_ELEMENT || !"samples".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <samples> not found.", toReadFrom.getLocation());
        }
        toReadFrom.next();
        
        // read into close samples element
        if (toReadFrom.getEventType() != XMLStreamConstants.END_ELEMENT || !"samples".equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </samples> not found.", toReadFrom.getLocation());
        }
        
        // read into close sample page
        if (toReadFrom.next() != XMLStreamConstants.END_ELEMENT || !SAMPLE_PAGE_TAG.equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </samplepage> not found.", toReadFrom.getLocation());
        }
        
        this.samples.clear();
        this.samples.addAll(readSamples);
        this.startIndex = startSampleIndex;
        this.pageSize = maxPageSize;
        this.startTime = sampleStartTime;
        this.nanosecondInterval = sampleInterval;
        this.nextPage = nextPageUuid;
    }
    
    /**
     * Reads a single text element from XMLStreamReader.
     * @param name The name of the element.
     * @param toReadFrom The reader to read from.
     * @return The string read from the stream.
     * @throws XMLStreamException If an exception occurs reading from stream.
     */
    private String readElement(String name, XMLStreamReader toReadFrom) throws XMLStreamException {
        // read starting element
        if (toReadFrom.next() != XMLStreamConstants.START_ELEMENT || !name.equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected <" + name + "> not found.", toReadFrom.getLocation());
        }
        
        if (toReadFrom.getEventType() != XMLStreamConstants.CHARACTERS) {
            throw new XMLStreamException("Expected " + name + " value not found.", toReadFrom.getLocation());
        }
        
        String value  = toReadFrom.getText();
        
        // read into close element
        if (toReadFrom.getEventType() != XMLStreamConstants.END_ELEMENT || !name.equals(toReadFrom.getLocalName())) {
            throw new XMLStreamException("Expected </" + name + "> not found.", toReadFrom.getLocation());
        }
        
        return value;
    }
}
