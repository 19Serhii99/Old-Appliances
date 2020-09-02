$(document).on('mouseover','[data-toggle="tooltip"]', function() {
    $(this).tooltip('show');
});

$(document).on('mouseup','[data-toggle="tooltip"]', function() {
    $(this).tooltip('hide');
});

let basketAmountVisibility = {
		visibility : false
}
const app = angular.module("appliances", ['ngCookies']);

app.controller("navbarController", ($scope, $http, $window, $cookies) => {
	$http.get("/api/v1/categories").then(response => $scope.categories = loadCategories(response.data));
	checkAuthorization($scope, $http);
	loadBasketAmount($scope, $http);
	
	$scope.signOut = () => {
		if ($scope.isLoggedIn) {
			const userType = $scope.isUser ? "users" : "employees";
			$http.put("/api/v1/" + userType + "/sign-out").then(response => $window.location.reload());
		}
	};
	
	$scope.toLoginPage = () => {
		savePageToCookies($window, $cookies);
		$window.location.href = "/login";
	};
	
	$scope.toSignUpPage = () => {
		savePageToCookies($window, $cookies);
		$window.location.href = "/register";
	};
});

const checkAuthorization = ($scope, $http) => {
	$http.get("/api/v1/users/has-authorized").then(response => {
		if (response.data == false) {
			$scope.isUser = false;
			$http.get("/api/v1/employees/has-authorized").then(response => {
				$scope.isLoggedIn = response.data;
				$scope.isAdmin = response.data;
			});
		} else {
			$scope.isLoggedIn = true;
			$scope.isUser = true;
		}
	});
};

const savePageToCookies = ($window, $cookies) => {
	const path = $window.location.pathname;
	if (path != "/login" && path != "/register") $cookies.put("previousPage", $window.location.href);
};

const loadCategories = (data) => {
	const categories = [];
	
	loadParentCategories(categories, data);
	loadSubcategories(categories, data);
	
	return categories;
};

const loadParentCategories = (categories, data) => {
	data.forEach(category => tryToCreateCategory(categories, category));
	return categories;
};

const tryToCreateCategory = (categories, category) => {
	if (category.parent != null) return;
	
	category.subcategories = [];
	categories.push(category);
};

const loadSubcategories = (categories, data) => {
	data.forEach(category => tryToCreateSubcategory(categories, category));
};

const tryToCreateSubcategory = (categories, category) => {
	if (category.parent == null) return;
	
	const parent = findParentCategory(categories, category);
	parent.subcategories.push(category);
};

const findParentCategory = (categories, category) => {
	return categories.find(parent => category.parent.id == parent.id);
};

const loadBasketAmount = ($scope, $http) => {
	$http.get("/api/v1/basket/amount").then(response => {
		const amount = parseInt(response.data);
		
		angular.element(document.getElementById("shopping-cart-amount")).html(amount);
		basketAmountVisibility.visibility = amount > 0;
		$scope.showBasketAmount = basketAmountVisibility;
	});
}

app.controller("authController", ($scope, $http, $window, $cookies) => {
	$scope.signUp = () => {
		$scope.showLastNameMessage = false;
		$scope.showFirstNameMessage = false;
		$scope.showMiddleNameMessage = false;
		$scope.showPhoneMessage = false;
		$scope.showEmailMessage = false;
		$scope.showPasswordMessage = false;
		$scope.showPasswordReplayMessage = false;
		
		angular.element(document.getElementById("last-name")).removeClass("border-danger");
		angular.element(document.getElementById("first-name")).removeClass("border-danger");
		angular.element(document.getElementById("middle-name")).removeClass("border-danger");
		angular.element(document.getElementById("phone")).removeClass("border-danger");
		angular.element(document.getElementById("email")).removeClass("border-danger");
		angular.element(document.getElementById("password")).removeClass("border-danger");
		angular.element(document.getElementById("password-replay")).removeClass("border-danger");
		
		if ($scope.password != $scope.passwordReplay) {
			$scope.showPasswordReplayMessage = true;
			angular.element(document.getElementById("password-replay")).addClass("border-danger");
			return;
		}
		
		let data = {
			lastName : $scope.lastName,
			firstName : $scope.firstName,
			middleName : $scope.middleName,
			phone : $scope.phone,
			email : $scope.email,
			password : $scope.password
		};
		
		$http.post("/api/v1/users", data).then(response => {
			const previousPage = $cookies.get("previousPage");
			$window.location.href = previousPage != null ? previousPage : "/";
		}, response => {
			response.data.errors.forEach(error => {
				switch (error.fieldError) {
					case "LAST_NAME":
						$scope.showLastNameMessage = true;
						$scope.lastNameMessage = error.message;
						angular.element(document.getElementById("last-name")).addClass("border-danger");
						break;
					case "FIRST_NAME":
						$scope.showFirstNameMessage = true;
						$scope.firstNameMessage = error.message;
						angular.element(document.getElementById("first-name")).addClass("border-danger");
						break;
					case "MIDDLE_NAME":
						$scope.showMiddleNameMessage = true;
						$scope.middleNameMessage = error.message;
						angular.element(document.getElementById("middle-name")).addClass("border-danger");
						break;
					case "PHONE":
						$scope.showPhoneMessage = true;
						$scope.phoneMessage = error.message;
						angular.element(document.getElementById("phone")).addClass("border-danger");
						break;
					case "EMAIL":
						$scope.showEmailMessage = true;
						$scope.emailMessage = error.message;
						angular.element(document.getElementById("email")).addClass("border-danger");
						break;
					case "PASSWORD":
						$scope.showPasswordMessage = true;
						$scope.passwordMessage = error.message;
						angular.element(document.getElementById("password")).addClass("border-danger");
						break;
				}
			});
		});
	};
	
	$scope.signIn = () => {
		let data = {
			email : $scope.email,
			password : $scope.password
		};
		
		$http.post("/api/v1/employees/sign-in", data).then(response => {
			const previousPage = $cookies.get("previousPage");
			
			$window.location.href = previousPage != null ? previousPage : "/";
			
			$cookies.remove("lastName");
			$cookies.remove("firstName");
			$cookies.remove("middleName");
			$cookies.remove("phone");
			$cookies.remove("city");
			$cookies.remove("postOffice");
			$cookies.remove("isDataSaved");
		}, response => {
			$http.post("/api/v1/users/sign-in", data).then(response => {
				const previousPage = $cookies.get("previousPage");
				$window.location.href = previousPage != null ? previousPage : "/";
			}, response => {
				if (response.data == null) {
					$scope.message = "Server connection problem...";
				} else {
					$scope.message = response.data.message;
					angular.element(document.getElementById("email")).addClass("border-danger");
					angular.element(document.getElementById("password")).addClass("border-danger");
				}
				$scope.showErrorMessage = true;
			});
		});
	};
});

const showErrorMessage = () => {
	$("#error-message").modal("toggle");
};

const fetchCountries = ($scope, $http) => {
	$http.get("/api/v1/countries").then(response => {
		$scope.countries = response.data;
		
		$scope.countries.forEach(element => {
			element.defaultName = element.name;
			element.isNew = false;
			element.isDatabaseNew = false;
			element.saved = true;
		}, response => {
			$scope.modalErrorMessage = response.data.message;
			showErrorMessage();
		});
		
		$scope.countries.push({
			name : "",
			isNew : true,
			isDatabaseNew : true,
			saved : false,
		});
	});
};

const fetchBrands = ($scope, $http) => {
	$http.get("/api/v1/brands").then(response => {
		$scope.brands = response.data;
		
		$scope.brands.forEach( (element, index) => {
			element.defaultName = element.name;
			element.defaultCountry = element.country.name;
			element.isNew = false;
			element.isDatabaseNew = false;
			element.saved = true;
		}, response => {
			$scope.modalErrorMessage = response.data.message;
			showErrorMessage();
		});
		
		$scope.brands.push({
			name : "",
			country : {
				id : $scope.countries[0].id,
				name : $scope.countries[0].name
			},
			isNew : true,
			isDatabaseNew : true,
			saved : false
		});
		
		setBrandSelectBox($scope);
	});
};

const setBrandSelectBox = ($scope) => {
	if ($scope.brands.length > 0) $scope.brand = $scope.brands[0].name;
};

app.controller("adminProductsController", ($scope, $http, $window) => {
	
	$http.get("/api/v1/categories").then(response => {
		$scope.categories = loadCategories(response.data);
		
		if ($scope.categories.length > 0) $scope.category = $scope.categories[0].name;
		
		$scope.subcategory = [];
		
		$scope.showSubcategory = [];
		$scope.showSubcategory[0] = true;
		
		for (let i = 0; i < $scope.categories.length; i++) {
			$scope.categories[i].index = i;
			$scope.subcategory[i] = $scope.categories[i].subcategories[0].name;
		}
		
	}, response => {
		$scope.modalErrorMessage = response.data.message;
		showErrorMessage();
	});
	
	fetchCountries($scope, $http);
	fetchBrands($scope, $http);
	
	$scope.categoryChanged = () => {
		const currentCategory = $scope.categories.find(element => element.name == $scope.category);
		
		for (let i = 0; i < $scope.showSubcategory.length; i++) {
			$scope.showSubcategory[i] = false;
		}
		
		$scope.showSubcategory[currentCategory.index] = true;
	};
	
	$scope.createProduct = () => {
		const currentCategory = $scope.categories.find(element => element.name == $scope.category);
		const currentSubcategory = currentCategory.subcategories.find(element => element.name == $scope.subcategory[currentCategory.index]);
		const currentBrand = $scope.brands.find(element => element.name == $scope.brand);
		
		const data = {
			name : $scope.name,
			price : $scope.price,
			amount : $scope.amount,
			width : $scope.width,
			height : $scope.height,
			depth : $scope.depth,
			category : {
				id : currentSubcategory.id
			},
			brand : {
				id : currentBrand.id
			}
		};
		
		$http.post("/api/v1/products", data).then(response => {
			$("#warning-message").modal("toggle");
		}, response => {
			response.data.errors.forEach(error => {
				switch (error.fieldError) {
				case "NAME":
					$scope.nameMessage = error.message;
					$scope.showNameMessage = true;
					break;
				case "BRAND_ID":
					$scope.brandMessage = error.message;
					$scope.showBrandMessage = true;
					break;
				case "CATEGORY_ID":
					$scope.categoryMessage = error.message;
					$scope.showCategoryMessage = true;
					break;
				}
			});
		});
	};
	
	$scope.gotIt = () => {
		$window.location.reload();
	};
	
});

app.controller("countriesController", ($scope, $http) => {
	fetchCountries($scope, $http);
	
	$scope.countryChanged = (index) => {
		const country = $scope.countries[index];
		
		if (country.isNew) {
			country.isNew = false;
			$scope.countries.push({
				name : "",
				isNew : true,
				isDatabaseNew : true,
				saved : false,
			});
			return;
		}
		
		$scope.countries[index].saved = country.defaultName == country.name;
	};
	
	$scope.saveCountry = (index) => {
		const country = $scope.countries[index];
		
		if (country.defaultName != country.name) {
			if (!country.isDatabaseNew) {
				$http.put("/api/v1/countries/" + country.id + "/" + country.name).then(response => {
					country.saved = true;
					country.defaultName = country.name;
				}, response => {
					$scope.modalErrorMessage = response.data.message;
					showErrorMessage();
				});
				return;
			}
			
			$http.post("/api/v1/countries/", {name : country.name}).then(response => {
				country.id = response.data.id;
				country.isDatabaseNew = false;
				country.saved = true;
				country.defaultName = country.name;
			}, response => {
				$scope.modalErrorMessage = response.data.message;
				showErrorMessage();
			});
		}
		
	};
	
	$scope.removeCountry = (index) => {
		const country = $scope.countries[index];
		
		if (!country.isNew) {
			if (country.isDatabaseNew) {
				$scope.countries.splice(index, 1);
				return;
			}
			
			$http.delete("/api/v1/countries/" + country.id).then(response => {
				$scope.countries.splice(index, 1);
			}, response => {
				$scope.modalErrorMessage = response.data.message;
				showErrorMessage();
			});
		}
		
	};
});

app.controller("brandsController", ($scope, $http) => {
	fetchCountries($scope, $http);
	fetchBrands($scope, $http);
	
	$scope.brandChanged = (index) => {
		const brand = $scope.brands[index];
		
		if (brand.isNew) {
			brand.isNew = false;
			$scope.brands.push({
				name : "",
				country : {
					id : $scope.countries[0].id,
					name : $scope.countries[0].name
				},
				isNew : true,
				isDatabaseNew : true,
				saved : false
			});
			return;
		}
		
		if (!brand.isDatabaseNew) {
			brand.saved = brand.defaultName == brand.name && brand.country.name == brand.defaultCountry;
		}
	};
	
	$scope.brandCountryChanged = (index) => {
		const brand = $scope.brands[index];
		
		if (!brand.isDatabaseNew) {
			brand.saved = brand.defaultName == brand.name && brand.country.id == brand.defaultCountry.id;
		}
	};
	
	$scope.saveBrand = (index) => {
		const brand = $scope.brands[index];
		
		if (brand.isDatabaseNew) {
			let data = {
				name : brand.name,
				country : {
					id : $scope.countries.find(element => element.name == brand.country.name).id 
				}
			};
			
			$http.post("/api/v1/brands", data).then(response => {
				brand.id = response.data.id;
				brand.isDatabaseNew = false;
				brand.saved = true;
			}, response => {
				$scope.modalErrorMessage = response.data.message;
				showErrorMessage();
			});
			
			return;
		}
		
		let data = {
			id : brand.id,
			name : brand.name,
			country : {
				id : $scope.countries.find(element => element.name == brand.country.name).id
			}
		};
		
		$http.post("/api/v1/brands/update", data).then(response => {
			brand.saved = true;
		}, response => {
			$scope.modalErrorMessage = response.data.message;
			showErrorMessage();
		});
	};
	
	$scope.removeBrand = (index) => {
		const brand = $scope.brands[index];
		
		if (!brand.isNew) {
			if (!brand.isDatabaseNew) {
				$http.delete("/api/v1/brands/" + brand.id).then(response => {
					$scope.brands.splice(index, 1);
				}, response => {
					$scope.modalErrorMessage = response.data.message;
					showErrorMessage();
				});
				return;
			}
			$scope.brands.splice(index, 1);
		}
	};
});

app.controller("productsController", ($scope, $http, $window, $cookies) => {
	const url = new URLSearchParams($window.location.search);
	
	if (url.get("search") == null) {
		const categoryId = getCategoryId($window);
		const filter = makeFilterForApi($scope, $window);
		const filterRequest = "/api/v1/products/" + categoryId + filter;
		
		markVisibility($scope, url);
		
		$http.get(filterRequest).then(response => {
			if (response.data.length == 0) {
				$scope.categoryName = "No items";
			} else {
				$scope.products = response.data;
				$scope.categoryName = $scope.products[0].category.name;
			}
			
			$http.get("/api/v1/brands/" + categoryId).then(brandReponse => {
				$scope.brandFilter = [];
				$scope.productBrands = brandReponse.data;
				markBrands($scope, $window);
			});
			
			if (!setPrice($scope, $window)) {
				$http.get(filterRequest + "/min-price").then(response => {
					$scope.minPrice = parseInt(response.data);
					$scope.defaultMinPrice = parseInt(response.data);
				});
				
				$http.get(filterRequest + "/max-price").then(response => {
					$scope.maxPrice = parseInt(response.data) + 1;
					$scope.defaultMaxPrice = parseInt(response.data);
				});
			}
			
			$http.get("/api/v1/basket").then(response => {
				$scope.basket = response.data;
				
				if ($scope.basket.items != undefined) {
					$scope.products.forEach(product => {
						$scope.basket.items.forEach(item => {
							product.ordered = product.id == item.product.id;
						});
					});
				}
			});
			
			checkAuthorization($scope, $http);
		});
		
		$scope.sorted = () => {
			$scope.filterSettings.sort = $scope.sort;
			makeFilter($window, $scope, categoryId);
		};
		
		$scope.brandFiltered = (index, brandId) => {
			if (!$scope.brandFilter[index]) {
				$scope.filterSettings.brands.forEach((elem, ind) => {
					if (elem == brandId) {
						$scope.filterSettings.brands.splice(ind, 1);
					}
				});
			} else {
				const brandItem = $scope.filterSettings.brands.find(elem => elem == brandId);
				if (brandItem == null) {
					$scope.filterSettings.brands.push(brandId);
				}
			}
			makeFilter($window, $scope, categoryId);
		};
		
		$scope.priceFiltered = () => {
			$scope.filterSettings.minPrice = parseInt($scope.minPrice);
			$scope.filterSettings.maxPrice = parseInt($scope.maxPrice);
			makeFilter($window, $scope, categoryId);
		};
		
		$scope.resetPrice = () => {
			$scope.minPrice = $scope.defaultMinPrice;
			$scope.maxPrice = $scope.defaultMaxPrice;
			
			delete $scope.filterSettings.minPrice;
			delete $scope.filterSettings.maxPrice;
			
			makeFilter($window, $scope, categoryId);
		};
		
		$scope.hiddenFiltered = () => {
			if ($scope.showHidden) {
				$scope.filterSettings.visibility = true;
			} else {
				delete $scope.filterSettings.visibility;
			}
			makeFilter($window, $scope, categoryId);
		};
		
		$scope.resetFilter = () => {
			$window.location.href = "/products?category=" + categoryId;
		};
	} else {
		$scope.hideLeftSide = true;
		$scope.categoryName = "Search for \"" + url.get("search") + "\"";
		$scope.searchValue = url.get("search");
		$http.get("/api/v1/products/search?text=" + $scope.searchValue).then(response => {
			if (response.data.length == 0) {
				$scope.categoryName += ": no results";
			} else {
				$scope.products = response.data;
			}
		});
	}
	
	$scope.addToBasket = (productId, productName) => {
		$http.post("/api/v1/basket", { productId : productId }).then(response => {
			loadBasketAmount($scope, $http);
			
			$scope.action = "basket";
			$scope.actedProduct = productName;
			$scope.products.find(element => element.id == productId).ordered = true;
			
			$("#basket-message").modal("toggle");
		}, response => {
			$scope.modalErrorMessage = response.data.message;
			showErrorMessage();
		});
	};
	
	$scope.search = () => {
		if ($scope.searchValue != undefined && $scope.searchValue.length > 0)
			$window.location.href = "/products?search=" + $scope.searchValue;
	};
	
	$scope.tuckAway = (productId, productName) => {
		$scope.action = "tuck away";
		$scope.buttonAction = "Tuck away";
		$scope.actedProduct = productName;
		$scope.productId = productId;
		$("#warning-message").modal("toggle");
	};
	
	$scope.remove = (productId, productName) => {
		$scope.action = "remove";
		$scope.actionDescription = "Product can be removed only if it not used anywhere.";
		$scope.buttonAction = "Remove";
		$scope.actedProduct = productName;
		$scope.productId = productId;
		$("#warning-message").modal("toggle");
	};
	
	$scope.makeVisible = (productId, productName) => {
		$scope.action = "make visible";
		$scope.buttonAction = "Make visible";
		$scope.actedProduct = productName;
		$scope.productId = productId;
		$("#warning-message").modal("toggle");
	};
	
	$scope.initiateAction = () => {
		if ($scope.action == "tuck away") {
			$http.put("/api/v1/products/" + $scope.productId + "/false").then(response => {
				$window.location.reload();
			}, response => {
				$("#warning-message").modal("toggle");
				$scope.modalErrorMessage = response.data.message;
				showErrorMessage();
			});
		} else if ($scope.action == "remove") {
			$http.delete("/api/v1/products/" + $scope.productId).then(response => {
				$window.location.reload();
			}, response => {
				$("#warning-message").modal("toggle");
				$scope.modalErrorMessage = response.data.message;
				showErrorMessage();
			});
		} else if ($scope.action == "make visible") {
			$http.put("/api/v1/products/" + $scope.productId + "/true").then(response => {
				$window.location.reload();
			}, response => {
				$("#warning-message").modal("toggle");
				$scope.modalErrorMessage = response.data.message;
				showErrorMessage();
			});
		} else if ($scope.action == "basket") {
			$window.location.href = "/basket";
		}
	};
	
	$scope.openProduct = (id) => {
		$window.location.href = "/product?id=" + id;
	};
});

const markVisibility = ($scope, url) => {
	const visibility = url.get("visibility");
	if (visibility == null) return;
	
	if (visibility == 1) {
		$scope.filterSettings.visibility = visibility;
		$scope.showHidden = true;
	}
};

const getCategoryId = ($window) => {
	return new URLSearchParams($window.location.search).get("category");
}

const makeFilterForApi = ($scope, $window) => {
	const url = new URLSearchParams($window.location.search);
	
	convertSortFromUrl($scope, url.get("sort"));
	
	$scope.filterSettings = {
		sort : "None",
		brands : []
	};
	
	return formFilterForApi(url);
};

const formFilterForApi = (url) => {
	const sort = url.get("sort");
	const brandItems = url.get("brands");
	const price = url.get("price");
	const visibility = url.get("visibility");
	
	let filter = "";
	
	if (sort != null && sort != "None") {
		filter += "sort=" + sort;
	}
	
	if (brandItems != null) {
		if (filter.length > 0) filter += ";";
		filter += "brands=" + brandItems;
	}
	
	if (price != null) {
		if (filter.length > 0) filter += ";";
		filter += "price=" + price;
	}
	
	if (visibility != null) {
		if (filter.length > 0) filter += ";";
		filter += "visibility=" + visibility;
	}
	
	if (filter.length > 0) return "/" + filter;
	
	return filter;
};

const markBrands = ($scope, $window) => {
	let brands = new URLSearchParams($window.location.search).get("brands");
	if (brands == null) return;
	
	brands = brands.split(",");
	brands.forEach(elem => {
		$scope.productBrands.forEach((item, index) => {
			if (item.id == elem) {
				$scope.brandFilter[index] = true;
				$scope.filterSettings.brands.push(elem);
			}
		});
	});
};

const setPrice = ($scope, $window) => {
	let price = new URLSearchParams($window.location.search).get("price");
	if (price == null) return false;
	
	price = price.split(",");
	
	$scope.filterSettings.minPrice = price[0];
	$scope.filterSettings.maxPrice = price[1];
	
	$scope.minPrice = price[0];
	$scope.maxPrice = price[1];
	
	$scope.defaultMinPrice = price[0];
	$scope.defaultMaxPrice = price[1];
	
	return true;
};

const convertSortFromUrl = ($scope, sort) => {
	if (sort == null) {
		$scope.sort = "None";
	} else {
		switch (sort) {
		case "cheap-expensive":
			$scope.sort = "Cheap – Expensive";
			break;
		case "expensive-cheap":
			$scope.sort = "Expensive – Cheap";
			break;
		case "start-end":
			$scope.sort = "New – Old";
			break;
		case "end-start":
			$scope.sort = "Old – New";
			break;
		default:
			$scope.sort = "None";
		}
	}
};

const makeFilter = ($window, $scope, categoryId) => {
	const url = new URLSearchParams();
	convertSortToUrl($scope.sort, url);
	
	let brandList = "";
	
	for (let i = 0; i < $scope.filterSettings.brands.length; i++) {
		if (brandList.length > 0) brandList += ",";
		brandList += $scope.filterSettings.brands[i];
	}
	
	if (brandList.length > 0) url.set("brands", brandList);
	
	if ($scope.filterSettings.minPrice != undefined && $scope.filterSettings.maxPrice != undefined) {
		url.set("price", $scope.filterSettings.minPrice + "," + $scope.filterSettings.maxPrice);
	}
	
	if ($scope.filterSettings.visibility != undefined) {
		url.set("visibility", 1);
	}
	
	let urlFilter = url.toString().length > 0 ? "&" : "";
	urlFilter += url.toString();
	urlFilter = urlFilter.replace(/%2C/g, ",");
	
	$window.location.href = "/products?category=" + categoryId + urlFilter;
}

const convertSortToUrl = (sort, url) => {
	switch (sort) {
	case "None":
		url.delete("sort");
		break;
	case "Cheap – Expensive":
		url.set("sort", "cheap-expensive");
		break;
	case "Expensive – Cheap":
		url.set("sort", "expensive-cheap");
		break;
	case "New – Old":
		url.set("sort", "start-end");
		break;
	case "Old – New":
		url.set("sort", "end-start");
		break;
	}
};

app.controller("basketController", ($scope, $http, $window) => {
	$http.get("/api/v1/basket").then(response => {
		$scope.basket = response.data;
	});
	
	$scope.increase = (productId, index) => {
		$http.post("/api/v1/basket", { productId : productId }).then(response => {
			$scope.basket.items[index].amount += 1;
			$scope.basket.sum += $scope.basket.items[index].product.price;
		}, response => {
			$scope.modalErrorMessage = response.data.message;
			showErrorMessage();
		});
	};
	
	$scope.decrease = (productId, index) => {
		$http.put("/api/v1/basket/" + productId).then(response => {
			$scope.basket.items[index].amount -= 1;
			$scope.basket.sum -= $scope.basket.items[index].product.price;
			if ($scope.basket.items[index].amount == 0) {
				$scope.basket.items.splice(index, 1);
				loadBasketAmount($scope, $http);
			}
		});
	};
	
	$scope.remove = (productId, index) => {
		$http.delete("/api/v1/basket/" + productId).then(response => {
			$scope.basket.sum -= $scope.basket.items[index].product.price * $scope.basket.items[index].amount;
			$scope.basket.items.splice(index, 1);
			loadBasketAmount($scope, $http);
		}, response => {
			$scope.modalErrorMessage = response.data.message;
			showErrorMessage();
		});
	};
	
	$scope.makeOrder = () => {
		$window.location.href = "/make-order";
	};
});

app.controller("makeOrderController", ($scope, $http, $window, $cookies) => {
	const isDataSaved = $cookies.get("isDataSaved");
	
	if (isDataSaved == null || !isDataSaved) {
		$http.get("/api/v1/users/has-authorized").then(response => {
			if (response.data == false) {
			} else {
				$http.get("/api/v1/users/current").then(response => {
					$scope.user = response.data;
					
					$cookies.put("lastName", user.lastName);
					$cookies.put("firstName", user.firstName);
					$cookies.put("middleName", user.middleName);
					$cookies.put("phone", user.phone);
					$cookies.put("city", user.city);
					$cookies.put("postOffice", "");
				});
				$scope.isLoggedIn = true;
			}
		});
	} else {
		loadMakeOrderPageFromCookies($scope, $cookies);
	}
	
	$http.get("/api/v1/basket/").then(response =>{
		const items = response.data.items;
		let amount = 0;
		
		items.forEach(item => {
			amount += item.amount;
		});
		
		$scope.productAmount = amount;
		$scope.orderCost = response.data.sum;
	});
	
	$scope.makeOrder = () => {
		$scope.showLastNameMessage = false;
		$scope.showFirstNameMessage = false;
		$scope.showMiddleNameMessage = false;
		$scope.showPhoneMessage = false;
		$scope.showCityMessage = false;
		
		angular.element(document.getElementById("last-name")).removeClass("border-danger");
		angular.element(document.getElementById("first-name")).removeClass("border-danger");
		angular.element(document.getElementById("middle-name")).removeClass("border-danger");
		angular.element(document.getElementById("phone")).removeClass("border-danger");
		angular.element(document.getElementById("city")).removeClass("border-danger");
		
		$cookies.remove("lastName");
		$cookies.remove("firstName");
		$cookies.remove("middleName");
		$cookies.remove("phone");
		$cookies.remove("city");
		$cookies.remove("postOffice");
		$cookies.remove("isDataSaved");
		
		const data = {
				lastName : $scope.user.lastName,
				firstName : $scope.user.firstName,
				middleName : $scope.user.middleName,
				phone : $scope.user.phone,
				city : $scope.user.city,
				postOffice : $scope.postOffice
		};
		
		$http.post("/api/v1/orders", data).then(response => {
			$scope.orderNumber = response.data;
			
			
			$("#warning-message").modal("toggle");
		}, response => {
			response.data.errors.forEach(error => {
				switch(error.fieldError) {
				case "LAST_NAME":
					$scope.lastNameMessage = error.message;
					$scope.showLastNameMessage = true;
					angular.element(document.getElementById("last-name")).addClass("border-danger");
					break;
				case "FIRST_NAME":
					$scope.firstNameMessage = error.message;
					$scope.showFirstNameMessage = true;
					angular.element(document.getElementById("first-name")).addClass("border-danger");
					break;
				case "MIDDLE_NAME":
					$scope.middleNameMessage = error.message;
					$scope.showMiddleNameMessage = true;
					angular.element(document.getElementById("middle-name")).addClass("border-danger");
					break;
				case "PHONE":
					$scope.phoneMessage = error.message;
					$scope.showPhoneMessage = true;
					angular.element(document.getElementById("phone")).addClass("border-danger");
					break;
				case "CITY":
					$scope.cityMessage = error.message;
					$scope.showCityMessage = true;
					angular.element(document.getElementById("city")).addClass("border-danger");
					break;
				}
			});
		});
	};
	
	$scope.lastNameChanged = () => {
		saveOrderDate($scope, $cookies);
	};
	
	$scope.firstNameChanged = () => {
		saveOrderDate($scope, $cookies);
	};
	
	$scope.middleNameChanged = () => {
		saveOrderDate($scope, $cookies);
	};
	
	$scope.phoneChanged = () => {
		saveOrderDate($scope, $cookies);
	};
	
	$scope.cityChanged = () => {
		saveOrderDate($scope, $cookies);
	};
	
	$scope.postOfficeChanged = () => {
		saveOrderDate($scope, $cookies);
	};
	
	$scope.gotIt = () => {
		$window.location.href = "/";
	};
});

const saveOrderDate = ($scope, $cookies) => {
	$cookies.put("lastName", $scope.user.lastName);
	$cookies.put("firstName", $scope.user.firstName);
	$cookies.put("middleName", $scope.user.middleName);
	$cookies.put("phone", $scope.user.phone);
	$cookies.put("city", $scope.user.city);
	$cookies.put("postOffice", $scope.postOffice);
	$cookies.put("isDataSaved", true);
};

const loadMakeOrderPageFromCookies = ($scope, $cookies) => {
	const lastName = $cookies.get("lastName");
	const firstName = $cookies.get("firstName");
	const middleName = $cookies.get("middleName");
	const phone = $cookies.get("phone");
	const city = $cookies.get("city");
	const postOffice = $cookies.get("postOffice");
	
	$scope.user = {};
	
	if (lastName != null && lastName != undefined) {
		$scope.user.lastName = lastName;
	}
	
	if (firstName != null && firstName != undefined) {
		$scope.user.firstName = firstName;
	}
	
	if (middleName != null && middleName != undefined) {
		$scope.user.middleName = middleName;
	}
	
	if (phone != null && phone != undefined) {
		$scope.user.phone = phone;
	}
	
	if (city != null && city != undefined) {
		$scope.user.city = city;
	}
	
	if (postOffice != null && postOffice != undefined) {
		$scope.postOffice = postOffice;
	}
};

app.controller("productController", ($scope, $http, $window) => {
	const url = new URLSearchParams($window.location.search);
	const productId = url.get("id");
	
	checkAuthorization($scope, $http);
	
	$http.get("/api/v1/products/details/" + productId).then(response => {
		$scope.product = response.data;
		
		$scope.product.width = $scope.product.width.toFixed(2);
		$scope.product.height = $scope.product.height.toFixed(2);
		$scope.product.depth = $scope.product.depth.toFixed(2);
		$scope.product.weight = $scope.product.weight.toFixed(2);
		
		angular.element(document.getElementsByTagName("title")).text(response.data.name);
	});
	
	$http.get("/api/v1/basket").then(response => {
		response.data.items.forEach(item => {
			if (item.product.id == productId) {
				$scope.isInBasket = true;
			}
		});
	});
	
	$scope.addToBasket = (productId, productName) => {
		$http.post("/api/v1/basket", { productId : productId }).then(response => {
			loadBasketAmount($scope, $http);
			
			$scope.action = "basket";
			$scope.actedProduct = productName;
			$scope.isInBasket = true;
			
			$("#basket-message").modal("toggle");
		}, response => {
			$scope.modalErrorMessage = response.data.message;
			showErrorMessage();
		});
	};
	
	$scope.initiateAction = () => {
		if ($scope.action == "tuck away") {
			$http.put("/api/v1/products/" + $scope.productId + "/false").then(response => {
				$window.location.reload();
			}, response => {
				$("#warning-message").modal("toggle");
				$scope.modalErrorMessage = response.data.message;
				showErrorMessage();
			});
		} else if ($scope.action == "remove") {
			$http.delete("/api/v1/products/" + $scope.productId).then(response => {
				$window.location.reload();
			}, response => {
				$("#warning-message").modal("toggle");
				$scope.modalErrorMessage = response.data.message;
				showErrorMessage();
			});
		} else if ($scope.action == "make visible") {
			$http.put("/api/v1/products/" + $scope.productId + "/true").then(response => {
				$window.location.reload();
			}, response => {
				$("#warning-message").modal("toggle");
				$scope.modalErrorMessage = response.data.message;
				showErrorMessage();
			});
		} else if ($scope.action == "basket") {
			$window.location.href = "/basket";
		}
	};
});

app.controller("accountController", ($scope, $http, $window) => {
	$http.get("/api/v1/users/has-authorized").then(response => {
		if (response.data == false) {
			$http.get("/api/v1/employees/has-authorized").then(response => {
				if (response.data == false) {
					
				} else {
					$http.get("/api/v1/employees/current").then(response => {
						$scope.user = response.data;
						$scope.isLoggedIn = true;
						$scope.isAdmin = true;
						
						const birthday = new Date(Date.parse($scope.user.birthday));
						$scope.user.birthday = birthday;
					});
				}
			});
		} else {
			$http.get("/api/v1/users/current").then(response => {
				$scope.user = response.data;
				$scope.isLoggedIn = true;
				$scope.isUser = true;
				
				const birthday = new Date(Date.parse($scope.user.birthday));
				$scope.user.birthday = birthday;
			});
		}
	});
	
	$scope.saveData = () => {
		const birthday = $scope.user.birthday;
		
		const data = {
			lastName : $scope.user.lastName,
			firstName : $scope.user.firstName,
			middleName : $scope.user.middleName,
			phone : $scope.user.phone,
			email : $scope.user.email,
			city : $scope.user.city,
		};
		
		if (birthday != undefined && birthday.getYear() <= new Date().getFullYear()) {
			data.birthday = birthday;
		} else {
			data.birthday = null;
		}
		
		angular.element(document.getElementById("last-name")).removeClass("border-danger");
		angular.element(document.getElementById("first-name")).removeClass("border-danger");
		angular.element(document.getElementById("middle-name")).removeClass("border-danger");
		angular.element(document.getElementById("phone")).removeClass("border-danger");
		angular.element(document.getElementById("email")).removeClass("border-danger");
		angular.element(document.getElementById("city")).removeClass("border-danger");
		
		$scope.showLastNameMessage = false;
		$scope.showFirstNameMessage = false;
		$scope.showMiddleNameMessage = false;
		$scope.showPhoneMessage = false;
		$scope.showEmailMessage = false;
		$scope.showCityMessage = false;
		$scope.dataSaved = false;
		
		const status = $scope.isAdmin ? "employees" : "users";
		
		$http.post("/api/v1/" + status + "/update", data).then(response => {
			$scope.dataSaved = true;
		}, response => {
			response.data.errors.forEach(error => {
				switch (error.fieldError) {
				case "LAST_NAME":
					$scope.showLastNameMessage = true;
					$scope.lastNameMessage = error.message;
					angular.element(document.getElementById("last-name")).addClass("border-danger");
					break;
				case "FIRST_NAME":
					$scope.showFirstNameMessage = true;
					$scope.firstNameMessage = error.message;
					angular.element(document.getElementById("first-name")).addClass("border-danger");
					break;
				case "MIDDLE_NAME":
					$scope.showMiddleNameMessage = true;
					$scope.middleNameMessage = error.message;
					angular.element(document.getElementById("middle-name")).addClass("border-danger");
					break;
				case "PHONE":
					$scope.showPhoneMessage = true;
					$scope.phoneMessage = error.message;
					angular.element(document.getElementById("phone")).addClass("border-danger");
					break;
				case "EMAIL":
					$scope.showEmailMessage = true;
					$scope.emailMessage = error.message;
					angular.element(document.getElementById("email")).addClass("border-danger");
					break;
				case "CITY":
					$scope.showCityMessage = true;
					$scope.cityMessage = error.message;
					angular.element(document.getElementById("city")).addClass("border-danger");
					break;
				}
			});
		});
	};
	
	$scope.savePassword = () => {
		angular.element(document.getElementById("new-password")).removeClass("border-danger");
		angular.element(document.getElementById("repeat-password")).removeClass("border-danger");
		
		$scope.showPasswordErrorMessage = false;
		$scope.passwordSaved = false;
		
		if ($scope.newPassword == undefined) {
			return;
		} else {
			if ($scope.newPassword.length == 0) {
				return;
			}
		}
		
		if ($scope.newPassword != $scope.repeatPassword) {
			angular.element(document.getElementById("new-password")).addClass("border-danger");
			angular.element(document.getElementById("repeat-password")).addClass("border-danger");
			
			$scope.showPasswordErrorMessage = true;
			$scope.passwordErrorMessage = "Passwords do not match!";
		} else {
			const status = $scope.isAdmin ? "employees" : "users";
			$http.put("/api/v1/" + status + "/update-password/" + $scope.newPassword).then(response => {
				$scope.passwordSaved = true;
				$scope.newPassword = "";
				$scope.repeatPassword = "";
			}, response => {
				angular.element(document.getElementById("new-password")).addClass("border-danger");
				angular.element(document.getElementById("repeat-password")).addClass("border-danger");
				
				$scope.showPasswordErrorMessage = true;
				$scope.passwordErrorMessage = response.data.message;
			});
		}
	};
});

app.controller("orderController", ($scope, $http, $window) => {
	const url = new URLSearchParams($window.location.search);
	
	$scope.filter = {
		date: url.get("date"),
		status: url.get("status")
	};
	
	let filter = "";
	if ($scope.filter.date != null) filter += "date=" + $scope.filter.date;
	if ($scope.filter.status != null) {
		if ($scope.filter.date != null) filter += ";";
		filter += "status=" + $scope.filter.status;
	}
	
	if ($scope.filter.date != null) {
		const dates = $scope.filter.date.split(",");
		
		$scope.dateFrom = new Date(Date.parse(dates[0]));
		$scope.dateTo = new Date(Date.parse(dates[1]));
	}
	
	const apiURL = filter.length = 0 ? "/api/v1/orders" : "/api/v1/orders/" + filter; 
	
	$http.get(apiURL).then(response => {
		$scope.orders = response.data;
		$scope.orders.forEach(order => {
			const time = new Date(Date.parse(order.creationTime));
			order.creationTime = time.toLocaleDateString().replace(/\./gi, '/') + " " + time.toLocaleTimeString();
		});
	});
	
	$http.get("/api/v1/statuses").then(response => {
		$scope.statuses = response.data;
		if ($scope.filter.status != null) {
			const statusName = $scope.statuses.find(elem => elem.id == $scope.filter.status).name;
			$scope.statusCode = statusName;
		} else {
			$scope.statusCode = $scope.statuses[0].name;
		}
	});
	
	$scope.statusCodeChanged = () => {
		const status = $scope.statusCode;
		if (status == "New") {
			delete $scope.filter.status;
		} else {
			const statusId = $scope.statuses.find(elem => elem.name == status).id;
			$scope.filter.status = statusId;
		}
		makeOrderFilter($scope, $window);
	};
	
	$scope.resetDate = () => {
		delete $scope.filter.date;
		makeOrderFilter($scope, $window);
	};
	
	$scope.applyDate = () => {
		const dateFromStr = $scope.dateFrom;
		const dateToStr = $scope.dateTo;
		
		if (dateFromStr != undefined && dateToStr != undefined) {
			const dateFrom = new Date(dateFromStr);
			const dateTo = new Date(dateToStr);
			
			const dateFromRequest = dateFrom.getFullYear() + "-" + (dateFrom.getMonth() + 1) + "-" + dateFrom.getDate();
			const dateToRequest = dateTo.getFullYear() + "-" + (dateTo.getMonth() + 1) + "-" + dateTo.getDate();
			
			$scope.filter.date = dateFromRequest + "," + dateToRequest;
			makeOrderFilter($scope, $window);
		}
	};
	
	$scope.resetFilter = () => {
		$window.location.href = "/orders";
	};
	
	$scope.changeStatus = (orderId, status) => {
		if (status == 0 || status == 2) {
			$http.put("/api/v1/orders/" + orderId).then(response => {
				$window.location.reload();
			});
		} else if (status == 1) {
			$http.delete("/api/v1/orders/" + orderId).then(response => {
				$window.location.reload();
			});
		}
	};
});

const makeOrderFilter = ($scope, $window) => {
	const url = new URLSearchParams();
	
	if ($scope.filter.status != null && $scope.filter.status != undefined) {
		url.set("status", $scope.filter.status);
	}
	if ($scope.filter.date != null && $scope.filter.date != undefined) {
		url.set("date", $scope.filter.date);
	}
	
	const search = url.toString();
	const href = "/orders" + search.length == 0 ? "" : "?" + search;
	
	$window.location.href = href;
};