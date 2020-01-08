$package("com.kidscademy");

/**
 * Section of text organized into columns of fixed width.
 * 
 * @author Iulian Rotaru
 */
com.kidscademy.ColumnText = class extends js.dom.Element {
	/**
	 * Construct an instance of ColumnText class.
	 * 
	 * @param js.dom.Document ownerDoc element owner document,
	 * @param Node node native {@link Node} instance.
	 * @assert assertions imposed by {@link js.dom.Element#Element(js.dom.Document, Node)}.
	 */
	constructor(ownerDoc, node) {
		super(ownerDoc, node);

		/** Paragraph end tag. */
		this.P_END_TAG = "</p>";

		/**
		 * Index of the next fragment to extract from text.
		 * 
		 * @type Number
		 */
		this._textIndex = 0;

		/**
		 * Column text container height. This is the height to which a column is allowed to grow.
		 * 
		 * @type Number
		 */
		this._containerHeight = 0;

		/**
		 * Current column.
		 * 
		 * @type js.dom.Element
		 */
		this._column = null;

		/**
		 * A column width, including actual width, border, padding and margin. This value is computed on the fly for the
		 * first created column and reused so that all columns have the same width.
		 * 
		 * @type Number
		 */
		this._columnWidth = 0;

		/**
		 * Column extra vertical space includes padding, border and margin. This value is computed on the fly for the first
		 * created column and reused for all columns.
		 * 
		 * @type Number
		 */
		this._columnExtraHeight = 0;

		/**
		 * Columns count.
		 * 
		 * @type Number
		 */
		this._columnsCount = 0;

		/**
		 * Cached value for estimation on minimum height required to insert a new paragraph into column. It includes bottom
		 * margin for current paragraph and at lest on line height for the newly one. This value is computed on the fly by
		 * {@link #this._estimateParagraphHeight()}.
		 * 
		 * @type Number
		 */
		this._minParagraphHeight = 0;

		/**
		 * Overflow tags are tags removed from overflowing paragraph. When adding paragraphs to current column it can happen
		 * that column height to exceed container, that is, overflow occurs. We need to remove words from paragraph end till
		 * overflow ends. Between removed word we can find formating tags like &lt;q&gt;. Store discovered tags into this
		 * array in order to restore them into next column.
		 * 
		 * @type Array
		 */
		this._overflowTags = [];
	}

	/**
	 * Set this column text content. Columns are created dynamically and filled up till parent container height limit.
	 * <p>
	 * Note that this method is a setter and will erase existing content, if any.
	 * 
	 * @param String text formatted text.
	 * @return com.kidscademy.ColumnText this object.
	 */
	setObject(text) {
		if (this.hasCssClass("hidden")) {
			return;
		}

		this._containerHeight = this.style.getHeight();
		$assert(this._containerHeight > 0, "com.kidscademy.ColumnText#ColumnText", "Container height is zero.\r\nPlease ensure column text parent has height set or inherited.");

		// ensure offset is zeroed on every text load
		this.style.setLeft(0);

		this.removeChildren();
		this._textIndex = 0;
		this._column = null;
		this._columnsCount = 0;

		var paragraphFragment, paragraphElement;
		while ((paragraphFragment = this._getParagraphFragment(text)) != null) {
			if (this._containerHeight - this._columnHeight() < this._estimateParagraphHeight()) {
				this._createColumn();
			}
			paragraphElement = this._addParagraph(paragraphFragment);

			if (this._isOverflow()) {
				// overflow tags stack contains tags removed from overflowing paragraph and restored into next column
				// first tag from stack should be paragraph itself in order to simplify restoring
				this._overflowTags.push("p");

				this._textIndex -= this.P_END_TAG.length;
				while (this._isOverflow()) {
					// remove last word by reading paragraph inner HTML, processing then written back to DOM
					// this may sound like brute force but is executed only for last paragraph
					// and, at least for now, I do not have better idea

					// the second parameter from remove last word method is the number of formating tags the paragraph
					// may have; it does not include <p> that was inserted before entering this loop

					// when insert newly fragment with overflowing tags removed browser create DOM elements as usual,
					// despite the missing end tag; I rely on this behavior
					// when extracting back for another last word removing, removed overflowing tags are present on HTML
					// fragment, generated by browser from DOM - need to instruct remove last word method to skip them

					paragraphElement.setHTML(this._removeLastWord(paragraphElement.getHTML(), this._overflowTags.length - 1));

					// rollback text index with the amount of last removed word
					this._textIndex -= this._lastWordLength;
				}
			}
		}

		this.style.setWidth(this._columnsCount * this._columnWidth);
		this.removeCssClass("closed");
	}

	getColumnsCount() {
		$assert(this._columnsCount > 0, "com.kidscademy.ColumnText#getColumnsCount", "Invalid state. Zero columns count.");
		return this._columnsCount;
	}

	setOffset(offset) {
		$assert(this._columnWidth > 0, "com.kidscademy.ColumnText#setOffset", "Invalid state. Zero column width.");
		$assert(offset >= 0 && offset < this._columnsCount, "com.kidscademy.ColumnText#setOffset", "Offset |%d| not in range.", offset);

		if (offset >= 0 && offset < this._columnsCount) {
			// this.style.setLeft(-offset * this._columnWidth + 100);
			this.style.setLeft(-offset * this._columnWidth);
		}

		var columns = this.getChildren();
		var columnsIndex = 0;
		for (; columnsIndex < offset; ++columnsIndex) {
			columns.item(columnsIndex).addCssClass("hidden");
		}
		for (; columnsIndex < columns.size(); ++columnsIndex) {
			columns.item(columnsIndex).removeCssClass("hidden");
		}
	}

	/**
	 * Get next paragraph fragment from given <code>text</code> or null if no more. Returned paragraph fragment starts
	 * from current text index till first paragraph end. Returns null if there are no more paragraphs.
	 * <p>
	 * If this method is called after column overflow and overflow tags are not empty, see {@link #_overflowTags}, this
	 * method insert them in front of returned paragraph, in order to restore removed overflow tags. If this is the case
	 * we are at the beginning of new column and removed overflow tags are from previous column.
	 * <p>
	 * This method has side effects: updates text index property, see {@link #_textIndex}.
	 * 
	 * @param String text formatted text.
	 * @return String paragraph fragment or null.
	 * @see #_textIndex
	 */
	_getParagraphFragment(text) {
		var startIndex = this._textIndex;
		this._textIndex = text.indexOf(this.P_END_TAG, this._textIndex);
		if (this._textIndex === -1) {
			return null;
		}
		this._textIndex += this.P_END_TAG.length;

		var paragraph = text.substring(startIndex, this._textIndex);

		// if removed tags is not empty we are after a column overflow
		// we need to remove spaces from paragraph start and insert removed tags
		if (this._overflowTags.length > 0) {
			paragraph = paragraph.trim();
			while (this._overflowTags.length) {
				paragraph = "<" + this._overflowTags.pop() + " class='overflow'>" + paragraph;
			}
		}

		return paragraph;
	}

	/**
	 * Test if current column content overflows. Is considered overflow if current column height is strictly greater
	 * than container height.
	 * 
	 * @return Boolean true if current column overflows.
	 */
	_isOverflow() {
		return this._columnHeight() > this._containerHeight;
	}

	/**
	 * Get current column height or zero if there is no current column created yet. Returned value include the effective
	 * height used by column text and also vertical padding, border and margin.
	 * 
	 * @return Number current column height.
	 */
	_columnHeight() {
		return this._column != null ? (this._column.style.getHeight() + this._columnExtraHeight) : 0;
	}

	/**
	 * Estimate minimum vertical space needed to insert a new paragraph. To be able to insert a new paragraph a column
	 * should have vertical space for current paragraph bottom margin and at lest one line for newly paragraph.
	 * <p>
	 * This method uses first paragraph line height as a hint for the new one. This means all paragraphs from all column
	 * text should have the same line height.
	 * <p>
	 * Returns maximum number value if current column is null and zero if current column has not at least two
	 * paragraphs; needs two paragraphs in order to have bottom margin to first.
	 * 
	 * @return Number minimum height needed for a new paragraph.
	 */
	_estimateParagraphHeight() {
		if (this._column == null) {
			return Number.MAX_VALUE;
		}

		var paragraph = this._column.getFirstChild();
		if (paragraph == null) {
			return 0;
		}

		if (this._minParagraphHeight === 0) {
			var lineHeight = parseInt(paragraph.style.getComputedStyle("line-height"));
			var paragraphBottomMargin = paragraph.style.getMargin().bottom;
			if (paragraphBottomMargin === 0) {
				return 0;
			}
			this._minParagraphHeight = lineHeight + paragraphBottomMargin;
		}
		return this._minParagraphHeight;
	}

	/**
	 * Create a new column and update column left position. Left position is computed based on column width, that is
	 * updated lazily on first column creation, see {@link #_columnWidth}. This method also updates columns count.
	 * <p>
	 * Note that column width is computed for first column and cached. This means all columns have the same width.
	 */
	_createColumn() {
		this._column = this._ownerDoc.createElement("div");
		this._column.addCssClass("column");
		this.addChild(this._column);

		if (this._columnWidth === 0) {
			var width = this._column.style.getWidth();
			var padding = this._column.style.getPadding();
			var border = this._column.style.getBorderWidth();
			var margin = this._column.style.getMargin();
			this._columnWidth = width + padding.left + padding.right + border.left + border.right + margin.left + margin.right;
			this._columnExtraHeight = padding.top + padding.bottom + border.top + border.bottom + margin.top + margin.bottom;
		}

		// for left value uses columns count existing before adding this column
		this._column.style.setLeft(this._columnsCount * this._columnWidth);
		++this._columnsCount;
	}

	/**
	 * Add paragraph described by given fragment to current column.
	 * 
	 * @param String paragraphFragment HTML fragment describing the paragraph to add.
	 * @return js.dom.Element newly created paragraph.
	 * @assert current column is not null.
	 */
	_addParagraph(paragraphFragment) {
		$assert(this._column !== null, "com.kidscademy.ColumnText#_addParagraph", "Current column is null.");
		this._column.addHTML(paragraphFragment);
		return this._column.getLastChild().addCssClass("paragraph");
	}

	/**
	 * Remove last word from paragraph and update last word length instance property. This method parses given paragraph
	 * backward till white space and returns paragraphs without last word. If found a formatted tag updates overflow
	 * tags stack, see {@link #_overflowTags}.
	 * 
	 * @param String paragraph paragraph to remove last word from,
	 * @param Array skipTags tags to skipped from paragraph start.
	 * @return String paragraph with last word removed.
	 */
	_removeLastWord(paragraph, skipTags) {
		var tag, buildTag;

		var skipTagsIndex = paragraph.length;
		while (skipTags) {
			skipTagsIndex = paragraph.lastIndexOf('<', skipTagsIndex - 1);
			--skipTags;
		}
		if (skipTagsIndex < paragraph.length) {
			paragraph = paragraph.substring(0, skipTagsIndex);
		}

		for (var i = paragraph.length - 1, char; i > 0; --i) {
			char = paragraph.charAt(i);
			switch (char) {
				case '>':
					buildTag = true;
					tag = "";
					break;

				case '<':
					if (buildTag) {
						buildTag = false;
						this._overflowTags.pop();
					}
					break;

				case '/':
					buildTag = false;
					this._overflowTags.push(tag);
					break;

				default:
					if (buildTag) {
						tag = char + tag;
					}
					break;

				case ' ':
					this._lastWordLength = paragraph.length - i;
					return paragraph.substring(0, i);
			}
		}
	}

	/**
	 * Class string representation.
	 * 
	 * @return this class string representation.
	 */
	toString() {
		return "com.kidscademy.ColumnText";
	}
};
