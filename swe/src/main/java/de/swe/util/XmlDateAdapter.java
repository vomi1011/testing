package de.swe.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class XmlDateAdapter extends XmlAdapter<String, Date> {
	private final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	
	@Override
	public String marshal(Date date) {
		return formatter.format(date);
	}
	
	@Override
	public Date unmarshal(String date) throws ParseException {
		return formatter.parse(date);
	}
}
