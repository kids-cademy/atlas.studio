$package("com.kidscademy.format");

com.kidscademy.format.HDateFormat = class {
	constructor() {
	}

	format(hdate) {
		switch (this.getFormat(hdate)) {
			case "YEAR":
				return `${hdate.value} ${this.getEra(hdate)}`;

			case "DECADE":
				return `${hdate.value}0s ${this.getEra(hdate)}`;

			case "CENTURY":
				const value = hdate.value;
				const era = this.getEra(hdate);
				const suffix = this.suffix(hdate.value);

				switch (this.getPeriod(hdate)) {
					case "BEGINNING":
						return `Beginning of ${value}${suffix} Century, ${era}`;

					case "MIDDLE":
						return `Middle of ${value}${suffix} Century, ${era}`;

					case "END":
						return `End of ${value}${suffix} Century, ${era}`;

					default:
						return `${value}${suffix} Century, ${era}`;
				}
				break;

			case "KYA":
				return `${hdate.value} kilo years ago`;

			case "MYA":
				return `${hdate.value} million years ago`;

			case "BYA":
				return `${hdate.value} billion years ago`;

			default:
				break;
		}

		return "";
	}

	roman(number) {
		const symbols = com.kidscademy.format.HDateFormat.RomanSymbols;
		var value = '';

		for (var i of Object.keys(symbols)) {
			var q = Math.floor(number / symbols[i]);
			number -= q * symbols[i];
			value += i.repeat(q);
		}

		return value;
	}

	suffix(value) {
		switch (value) {
			case 1:
				return "st";

			case 2:
				return "nd";

			case 3:
				return "rd";

			default:
				return "th";
		}
	}

	getFormat(hdate) {
		if (!hdate.mask) {
			return null;
		}
		return com.kidscademy.format.HDateFormat.Format[hdate.mask & 0x000000FF];
	}

	getPeriod(hdate) {
		if (!hdate.mask) {
			return null;
		}
		return com.kidscademy.format.HDateFormat.Period[(hdate.mask & 0x0000FF00) >> 8];
	}

	getEra(hdate) {
		if (!hdate.mask) {
			return null;
		}
		return com.kidscademy.format.HDateFormat.Era[(hdate.mask & 0x00FF0000) >> 16];
	}

	parse(value) {
		return null;
	}

	test(value) {
		return !!value;
	}

	toString() {
		return "com.kidscademy.format.HDateFormat";
	}
};

com.kidscademy.format.HDateFormat.Format = ["DATE", "YEAR", "DECADE", "CENTURY", "MILLENNIA", "KYA", "MYA", "BYA"];

com.kidscademy.format.HDateFormat.Period = ["FULL", "BEGINNING", "MIDDLE", "END"];

com.kidscademy.format.HDateFormat.Era = ["CE", "BCE"];

com.kidscademy.format.HDateFormat.RomanSymbols = {
	M: 1000,
	CM: 900,
	D: 500,
	CD: 400,
	C: 100,
	XC: 90,
	L: 50,
	XL: 40,
	X: 10,
	IX: 9,
	V: 5,
	IV: 4,
	I: 1
};
