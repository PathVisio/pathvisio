// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
class Token
{
	static final int F_NUMBER = 0x01; // double, int or flag
	static final int F_DOUBLE = 0x02; // double, not int
	static final int F_NOT_DOUBLE = 0x04; // int or flag, not double
	static final int F_FLAG = 0x08; // flag, not int or double
	static final int F_COMMAND = 0x10; // command
	static final int F_WHITESPACE_OR_COMMA = 0x20; // whitespace or comma
	static final int F_COMMA = 0x40; // comma, not whitespace
	static final int F_WHITESPACE = 0x80; // whitespace, not comma
	static final int F_END = 0x100;

	private int flags = 0;
	private double doubleValue = 0.0;
	private char command = ' ';

	/**
	   constructor for number tokens
	*/
	Token (int _flags, double _value)
	{
		assert ((_flags & (F_NUMBER | F_DOUBLE | F_NOT_DOUBLE | F_FLAG)) == _flags);
		doubleValue = _value;
		flags = _flags;
	}

	/**
	   constructor for command tokens
	*/
	Token (char _command)
	{
		flags = F_COMMAND;
		command = _command;
	}

	/**
	   constructor for whitespace, comma or end tokens
	*/
	Token (int _flags)
	{
		assert ((_flags & (F_COMMA | F_WHITESPACE_OR_COMMA | F_WHITESPACE | F_END)) == _flags);
		flags = _flags;
	}

	int getFlags () { return flags; }
	double getDouble () { return doubleValue; }
	boolean getBoolean () { return (Math.round (doubleValue) == 1); }
	char getCommand () { return command; }
	boolean testFlag (int value) { return ((flags & value) > 0); }
}
