/*
 *    Fernflower - The Analytical Java Decompiler
 *    http://www.reversed-java.com
 *
 *    (C) 2008 - 2010, Stiver
 *
 *    This software is NEITHER public domain NOR free software 
 *    as per GNU License. See license.txt for more details.
 *
 *    This software is distributed WITHOUT ANY WARRANTY; without 
 *    even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 *    A PARTICULAR PURPOSE. 
 */

package de.fernflower.modules.decompiler;

import java.util.ArrayList;
import java.util.List;

import de.fernflower.modules.decompiler.stats.Statement;

public class StatEdge {
	
	public static final int TYPE_ALL = 0xFF;
	
	public static final int TYPE_REGULAR = 1;
	public static final int TYPE_EXCEPTION = 2;
	public static final int TYPE_BREAK = 4;
	public static final int TYPE_CONTINUE = 8;
	public static final int TYPE_FINALLYEXIT = 32;

	public static final int[] TYPES = new int[] {
		TYPE_REGULAR,
		TYPE_EXCEPTION,
		TYPE_BREAK,
		TYPE_CONTINUE,
		TYPE_FINALLYEXIT
	};
	
	private int type;
	
	private Statement source;
	
	private Statement destination;
	
	private List<String> exceptions;
	
	public Statement closure;
	
	public boolean labeled = true;

	public boolean explicit = true;
	
	public StatEdge(int type, Statement source, Statement destination, Statement closure) {
		this(type, source, destination);
		this.closure = closure;
	}

	public StatEdge(int type, Statement source, Statement destination) {
		this.type = type;
		this.source = source;
		this.destination = destination; 
	}

	public StatEdge(Statement source, Statement destination, List<String> exceptions) {
		this(TYPE_EXCEPTION, source, destination);
		if(exceptions != null) {
			this.exceptions = new ArrayList<String>(exceptions);
		}
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Statement getSource() {
		return source;
	}

	public void setSource(Statement source) {
		this.source = source;
	}

	public Statement getDestination() {
		return destination;
	}

	public void setDestination(Statement destination) {
		this.destination = destination;
	}

	public List<String> getExceptions() {
		return this.exceptions;
	}

//	public void setException(String exception) {
//		this.exception = exception;
//	}
}
