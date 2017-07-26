/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.android.bookfinder;

/**
 * An {@link Book} object contains information related to a single earthquake.
 */
public class Book {

    /**
     * Title of the book
     */
    private String title;

    /**
     * Author of the book
     */
    private String author;

    /**
     * Image URL of the book
     */
    private String imageUrl;

    /**
     * Website URL of the book
     */
    private String bookUrl;

    /**
     * Price of book
     */
    private String bookPrice;

    /**
     * Constructs a new {@link Book} object.
     *
     * @param ttitle     title of book
     * @param aauthor    author of book
     * @param iimageUrl  url of thumbnail url
     * @param bbookUrl   is the website URL to find more details about the book
     * @param bbookPrice is the price of the book
     */
    public Book(String ttitle, String aauthor, String iimageUrl, String bbookUrl, String bbookPrice) {
        title = ttitle;
        author = aauthor;
        imageUrl = iimageUrl;
        bookUrl = bbookUrl;
        bookPrice = bbookPrice;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getBookUrl() {
        return bookUrl;
    }

    public String getBookPrice() {
        return bookPrice;
    }
}
