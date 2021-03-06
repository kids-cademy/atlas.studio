$package("com.kidscademy");

com.kidscademy.Htm2Md = class {
    constructor(sectionElement) {
        this._elements = sectionElement.getChildren();
        this._sectionName = sectionElement.getName();
    }

    sectionName() {
        return this._sectionName;
    }

    converter() {
        var markdown = "";
        for (let i = 0; i < this._elements.size(); ++i) {
            const element = this._elements.item(i);

            switch (element.getTag()) {
                case "h1":
                    markdown += `# ${this._markdown(element)}`;
                    break;

                case "h2":
                    markdown += `## ${this._markdown(element)}`;
                    break;

                case "p":
                    markdown += this._markdown(element);
                    break;

                case "ul":
                    for (let j = 0; ;) {
                        markdown += `- ${this._markdown(element.getByIndex(j))}`;
                        if (++j === element.getChildrenCount()) {
                            break;
                        }
                        markdown += "\r\n";
                    }
                    break;

                case "ol":
                    for (let j = 0; ;) {
                        markdown += `${j + 1}. ${this._markdown(element.getByIndex(j))}`;
                        if (++j === element.getChildrenCount()) {
                            break;
                        }
                        markdown += "\r\n";
                    }
                    break;

                case "table":
                    const layout = [];
                    const columnWidths = [];
                    const rows = element.findByTag("tr");

                    for (let i = 0; i < rows.size(); ++i) {
                        layout[i] = rows.item(i).getChildren().map(td => td.getText().trim());
                        for (let j = 0; j < layout[i].length; ++j) {
                            columnWidths[j] = Math.max(columnWidths[j] || 0, layout[i][j].length);
                        }
                    }

                    function text(text, width, padding) {
                        text = text.trim();
                        while (text.length < width) {
                            text += padding;
                        }
                        return text;
                    }

                    for (let j = 0; j < layout[0].length; ++j) {
                        markdown += `| ${text(layout[0][j], columnWidths[j], ' ')} `;
                    }
                    markdown += "\r\n";

                    for (let j = 0; j < layout[0].length; ++j) {
                        markdown += `|-${text("", columnWidths[j], '-')}-`;
                    }
                    markdown += "\r\n";

                    if (layout.length > 1) {
                        for (let i = 1; ;) {
                            for (let j = 0; j < layout[i].length; ++j) {
                                markdown += `| ${text(layout[i][j], columnWidths[j], ' ')} `;
                            }
                            if (++i == layout.length) {
                                break;
                            }
                            markdown += "\r\n";
                        }
                    }
                    break;
            }
            markdown += "\r\n\r\n";
        }
        return markdown;
    }

    /**
     * Extract entire text content from given element, handling inline formatting elements. This method
     * traverse recursivelly given element and all its descendant. Currently recognized inline formatting 
     * elements are <em> and <strong>.
     * 
     * This method accepts both j(s)-lib elements and ECMA nodes. If argument is js.dom.Element convert 
     * it to ECMA Node.
     * 
     * @param {js.dom.Element | Node} element source DOM element; accepts both j(s)-lib elements and ECMA nodes.
     * @return {String} element text content formatted as markdown.
     */
    _markdown(element) {
        // this method implemntation uses ECMA nodes and need to convert j(s)-lib elements
        if (typeof element.nodeType === "undefined") {
            element = element.getNode();
        }

        var markdown = "";
        const children = element.childNodes;
        for (let i = 0; i < children.length; ++i) {
            let child = children.item(i);
            switch (child.nodeType) {
                case Node.TEXT_NODE:
                    markdown += child.nodeValue;
                    break;

                case Node.ELEMENT_NODE:
                    switch (child.tagName.toLowerCase()) {
                        case "strong":
                            markdown += `\`${this._markdown(child)}\``;
                            break;

                        case "em":
                            markdown += `"${this._markdown(child)}"`;
                            break;
                    }
                    break;
            }
        }
        return markdown;
    }

    toString() {
        return "com.kidscademy.Htm2Md";
    }
};
