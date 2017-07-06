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
import java.util.Iterator;
import java.util.UUID;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.apache.logging.log4j.LogManager;

/**
 * This class provides SamplePage management functionality.
 */
class SamplePageManager {
    /**
     * Class logger instance.
     */
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    
    /**
     * The number of samples for each page to hold.
     */
    private final int pageSize;
    
    /**
     * The location to read and write this instance to.
     */
    private final UuidStringLocation store;
    
    /**
     * The first page in the linked list of pages.
     */
    private SamplePage firstPage;
    
    /**
     * The UUID where the firstPage is stored.
     */
    private UUID firstPageLocation;
    
    /**
     * The last page in the linked list of pages.
     */
    private UUID lastPageLocation;
    
    /**
     * The last page in the linked list of pages.
     */
    private SamplePage lastPage;
    
    /**
     * The number of pages in this list.
     */
    private int pageCount;
    
    /**
     * The start time of the first sample.
     */
    private final Instant startTime;
    
    /**
     * The interval between samples.
     */
    private final long nanosecondInterval;
    
    /**
     * Initializes a new instance of the SamplePageManager class.
     * @param store The location to store this instant at.
     * @param pageSize The number of samples per page.
     * @param startTime The time of the first sample.
     * @param nanosecondInterval The interval between samples in nanoseconds.
     */
    SamplePageManager(UuidStringLocation store, int pageSize, Instant startTime, long nanosecondInterval) {
        this.store = store;
        this.pageSize = pageSize;
        this.startTime = startTime;
        this.nanosecondInterval = nanosecondInterval;

        this.firstPageLocation = UUID.randomUUID();
        this.lastPageLocation = this.firstPageLocation;
        // create a first empty page
        this.firstPage = new SamplePage(
                this.store.getNewLocation(this.firstPageLocation),
                0,
                this.pageSize,
                startTime,
                nanosecondInterval);
        
        this.lastPage = firstPage;
        this.pageCount = 1;
    }
    
    /**
     * Adds a new sample.
     * @param toAdd The sample to add.
     */
    public void addSample(Sample toAdd) {
        if (lastPage.isFull()) {
            try {
                this.addPage();
            } catch (XMLStreamException ex) {
                LOGGER.error("Could not add a new page while adding sample.", ex);
            }
        }
        
        this.lastPage.addSample(toAdd);
    }
    
    /**
     * Gets the number of samples.
     * @return The number of samples.
     */
    public int getSampleCount() {
        // number of samples equals the number of full pages (pageCount - 1)
        // times the size of pages (pageSize) plust the number in the last page
        return this.lastPage.sampleCount() + ((this.pageCount - 1) * this.pageSize);
    }

    /**
     * Gets an iterator for the samples in this set.
     * @return 
     */
    Iterable<Sample> getSamples() {
        return () -> new Iterator<Sample>() {
            SamplePage currentPage = firstPage;
            int currentIndex = 0;
            
            @Override
            public boolean hasNext() {
                return currentIndex < currentPage.sampleCount();
            }
            
            @Override
            public Sample next() {
                // get the current sample
                Sample toReturn = currentPage.getSample(currentIndex);
                
                // increment the sample we're currently at
                currentIndex++;
                
                // if we incremented past the last sample in the page
                // go to the next page and reset the index
                if (currentIndex == currentPage.sampleCount()) {
                    this.currentPage = this.currentPage.getNextPage();
                    this.currentIndex = 0;
                }
                
                // return the current item
                return toReturn;
            }
        };
    }
    
    /**
     * Writes the manager to a XMLStreamWriter
     * @param toWriteTo the XMLStreamWriter to write to.
     * @throws XMLStreamException If an exception occurs while writing to the stream.
     */
    private void writeTo(XMLStreamWriter toWriteTo) throws XMLStreamException {
        toWriteTo.writeStartElement("firstpagelocation");
        toWriteTo.writeCharacters(this.firstPageLocation.toString());
        toWriteTo.writeEndElement();
        
        toWriteTo.writeStartElement("lastpagelocation");
        toWriteTo.writeCharacters(this.lastPageLocation.toString());
        toWriteTo.writeEndElement();
    }
    
    /**
     * Reads the manager from a XMLStreamReader.
     * @param toReadFrom the XMLStreamReader to read from.
     * @throws XMLStreamException If an exception occurs while reading from the stream.
     */
    private void readFrom(XMLStreamReader toReadFrom) throws XMLStreamException {
        //this.firstPageLocation
        UUID firstLocation = UUID.fromString(this.readElement("firstpagelocation", toReadFrom));
        //this.lastPageLocation
        UUID lastLocation = UUID.fromString(this.readElement("lastpagelocation", toReadFrom));
        
        // load the first page
        SamplePage newFirstPage = new SamplePage(
                this.store.getNewLocation(firstLocation),
                0,
                this.pageSize,
                this.startTime,
                this.nanosecondInterval);
        newFirstPage.load();
        
        // if the first and last are the same we are done
        if (firstLocation.equals(lastLocation)) {
            // save both newly loaded pages and locations
            this.firstPageLocation = firstLocation;
            this.lastPageLocation = firstLocation;
            this.firstPage = newFirstPage;
            this.lastPage = newFirstPage;
        } else {
            // otherwise load the last page as well
            SamplePage newLastPage = new SamplePage(
                this.store.getNewLocation(lastLocation),
                0,
                this.pageSize,
                this.startTime,
                this.nanosecondInterval);
            newLastPage.load();
            
            // save both newly loaded pages and locations
            this.firstPageLocation = firstLocation;
            this.lastPageLocation = lastLocation;
            this.firstPage = newFirstPage;
            this.lastPage = newLastPage;
        }
    }
    
    /**
     * Adds a new page.
     * @throws XMLStreamException If a exception occurs saving the current last page.
     */
    private void addPage() throws XMLStreamException {
        // create new page
        SamplePage newPage = this.lastPage.getNextPage();
        
        // save last page
        this.lastPage.save();
        
        // set last page to new page
        this.lastPage = newPage;
        
        // increment the number of pages in the list
        this.pageCount++;
    }

    /**
     * Saves this instance to persistent storage.
     * @throws XMLStreamException If an exception occurs while writing this instance.
     */
    void save() throws IOException {
        StringWriter sw = new StringWriter();
        try {
            XMLStreamWriter toWriteTo =
                    XMLOutputFactory.newFactory().createXMLStreamWriter(sw);
            this.writeTo(toWriteTo);
        } catch (XMLStreamException ex) {
            throw new IOException("IO Exception occured writing SamplePageManager from persistant storage.", ex);
        }
        this.store.write(sw.toString());
    }

    /**
     * Loads this instance from persistent storage.
     * @throws XMLStreamException If an exception occurs while reading this instance.
     */
    void load() throws IOException {
        try {
            // try to read in any previously saved data
            StringReader sr = new StringReader(this.store.read());
            XMLStreamReader toReadFrom = XMLInputFactory.newFactory().createXMLStreamReader(sr);
            this.readFrom(toReadFrom);
        } catch (XMLStreamException ex) {
            throw new IOException("IO Exception occured reading SamplePageManager from persistant storage.", ex);
        }
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
