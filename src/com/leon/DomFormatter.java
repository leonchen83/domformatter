package com.leon;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : Leon
 * @since : 2014-3-19
 * @see :
 */

public class DomFormatter {

	private int tab_count = 0;
	private int index = 0;
	private char[] c;
	private List<String> label_stack = new ArrayList<String>();

	public DomFormatter(String fileName, Class<?> clazz) {
		String str = FileUtils.readFile(fileName, clazz);
		c = str.toCharArray();
	}

	public static void main(String[] args) {
		DomFormatter formatter = new DomFormatter("test2.txt", DomFormatter.class);
		formatter.format();
		System.out.println(formatter.label_stack);
	}

	public void format() {
		label();
	}

	private void label() {
		ignore_white_space();
		if (current() == '<') {
			eat();
			ignore_white_space();
			if (current() == '!') {
				eat();
				print("\n", "<!", tab_count);
				ignore_comment();
				label();
			} else if (is_word(current())) {
				String label = word(current());
				if (is_script(label)) {
					print("\n<script");
					ignore_script();
					label();
				} else {
					print("\n", "<" + label, tab_count);
					plus_plus();
					ignore_white_space();
					push(label);
					label_attr();
				}
			} else if (current() == '/') {
				eat();
				ignore_white_space();
				if (is_word(current())) {
					String label_end = word(current());
					minus_minus();
					print("\n", "</" + label_end + ">", tab_count);
					if (top().equals(label_end.toLowerCase())) {
						pop();
					}
				}
				ignore_white_space();
				if (current() == '>') {
					eat();
					ignore_white_space();
					if (eof()) {
						return;
					}
					label_content();
				}
			}
		} else {
			throw new UnsupportedOperationException("char is " + current() + ",index is " + index);
		}

	}

	private void label_attr() {
		ignore_white_space();
		if (is_word(current())) {
			String attr = word(current());
			print(" " + attr);
			ignore_white_space();
			if (current() == '=') {
				eat();
				print("=");
				ignore_white_space();
				if (current() == '"') {
					arrt_value(current(), '"');
					label_attr();
				} else if (current() == '\'') {
					arrt_value(current(), '\'');
					label_attr();
				} else if (is_word(current())) {
					arrt_value(current());
					label_attr();
				}
			} else {
				if (is_minimized_attr(attr)) {
					label_attr();
				} else {
					throw new UnsupportedOperationException("missing attr_value,attr is '" + attr + "',index is " + index);
				}
			}
		} else if (current() == '/') {
			eat();
			print(" /");
			ignore_white_space();
			if (current() == '>') {
				eat();
				print(">");
				minus_minus();
				pop();
				label_content();
			}
		} else if (current() == '>') {
			print(">");
			eat();
			label_content();
		}
	}

	private void label_content() {
		ignore_white_space();
		if (current() == '<') {
			label();
		} else {
			content(current());
			label_content();
		}
	}

	private void ignore_script() {
		while (current() != '<') {
			ignore_double_quote_and_single_quote();
			print(current());
			eat();
		}
		print(current());
		eat();
		if (current() == '/') {
			print(current());
			eat();
			String word = word(current());
			if (is_script(word)) {
				ignore_white_space();
				if (current() == '>') {
					eat();
					print("script>\n");
				}
			} else {
				ignore_script();
			}
		} else {
			ignore_script();
		}
	}

	private void ignore_double_quote_and_single_quote() {
		if (current() == '"') {
			arrt_value(current(), '"');
		}
		if (current() == '\'') {
			arrt_value(current(), '\'');
		}
	}

	private void ignore_comment() {
		while (current() != '>') {
			ignore_double_quote_and_single_quote();
			if (current() == '\n') {
				eat();
				continue;
			}
			if (current() == '>') {
				break;
			}
			print(current());
			eat();
		}
		print(current());
		eat();
	}

	private void ignore_white_space() {
		while (ignore_letter()) {
			eat();
		}
	}

	private boolean ignore_letter() {
		return current() == ' ' || current() == '\r' || current() == '\n' || current() == '\t';
	}

	private boolean is_script(String label_node) {
		return label_node.toLowerCase().equals("script");
	}

	private boolean is_word(char c) {
		return Character.isLetter(c) || c == '-' || (c >= '0' && c <= '9') || c == '_' || c == ':' || c == '#';
	}

	private boolean is_minimized_attr(String attr) {
		String[] attrs = new String[] { "compact", "checked", "declare", "readonly", "disabled", "selected", "defer", "ismap", "nohref", "noshade", "nowrap", "multiple", "noresize", "allowfullscreen" };
		for (int i = 0; i < attrs.length; i++) {
			if (attr.toLowerCase().equals(attrs[i])) {
				return true;
			}
		}
		return false;
	}

	private String word(char d) {
		StringBuffer word = new StringBuffer();
		word.append(d);
		eat();
		while (is_word(current())) {
			word.append(current());
			eat();
		}
		return word.toString();
	}

	private void arrt_value(char d, char mode) {
		print(d);
		eat();
		while (current() != mode) {
			if (current() == '\\') {
				print("\\");
				eat();
				if (current() == mode) {
					print(mode);
					eat();
				}
			}
			print(current());
			eat();
		}
		print(current());
		eat();
	}

	private void arrt_value(char d) {
		print(d);
		eat();
		while (current() != ' ') {
			if (current() == '>') {
				break;
			}
			print(current());
			eat();
		}
	}

	private void content(char d) {
		print(d);
		eat();
		while (current() != '<') {
			print(current());
			eat();
		}
	}

	private void eat() {
		index++;
	}

	private char current() {
		if (index < c.length) {
			return c[index];
		}
		return 0;
	}

	private boolean eof() {
		return index == c.length;
	}

	private void print(String prefix, String value, int tab_count) {
		print(prefix);
		for (int i = 0; i < tab_count; i++) {
			print("\t");
		}
		print(value);
	}

	private void print(String value) {
		print(value.toCharArray());
	}

	private void print(char[] value) {
		System.out.print(value);
	}

	private void print(char c) {
		System.out.print(c);
	}

	private void plus_plus() {
		tab_count++;
	}

	private void minus_minus() {
		tab_count--;
	}

	private void push(String str) {
		label_stack.add(0, str.toLowerCase());
	}

	private String pop() {
		String str = top();
		label_stack.remove(0);
		return str;
	}

	private String top() {
		return label_stack.get(0);
	}
}
