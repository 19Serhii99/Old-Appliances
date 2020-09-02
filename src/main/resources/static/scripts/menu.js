let mouseover;
let timerID;
let isButtonOver;
let isMenuOver;

const buttonDown = () => {
	if (!mouseover) {
		mouseover = true;
		isButtonOver = true;
		timerID = animateMenu();
	}
};

const buttonUp = () => {
	if (mouseover) {
		mouseover = false;
		clearTimeout(timerID);
	}
	isButtonOver = false;
	tryToCloseMenu();
};

const menuDown = () => {
	isMenuOver = true;
	if (!mouseover) {
		mouseover = true;
		timerID = animateMenu();
	}
};

const animateMenu = () => {
	return setTimeout(() => {
		$(".menu").attr("class", "menu-hover");
		$(".menu-hover").bind("animationend webkitAnimationEnd oAnimationEnd MSAnimationEnd", () => {
			mouseover = false;
		});
	}, 200)
};

const menuUp = () => {
	if (mouseover) {
		mouseover = false;
		clearTimeout(timerID);
	}
	isMenuOver = false;
	tryToCloseMenu();
};

const tryToCloseMenu = () => {
	setTimeout(() => {
		if (!isMenuOver && !isButtonOver) {
			$(".menu-hover").stop(true, true);
			$(".menu-hover").attr("class", "menu");
		}
	}, 50);
};