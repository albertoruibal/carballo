package com.alonsoruibal.chess.book;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;

public interface BookClientBundle extends ClientBundle {

    public static final BookClientBundle INSTANCE = GWT.create(BookClientBundle.class);

    @Source("book_small.bin")
	public DataResource book_small();
}
