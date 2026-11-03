import collections
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PATH = os.path.join(ROOT, "app", "src", "main", "assets", "recipes.json")

CATEGORIES = {
    "家常菜": "home",
    "汤": "soup",
    "主食": "staple",
    "面点": "pastry",
    "凉菜": "cold",
    "甜品": "dessert",
    "西餐": "western",
    "小吃": "snack",
}


def main():
    with open(PATH, encoding="utf-8") as f:
        data = json.load(f)

    errors = []
    dishes = data.get("dishes", [])
    if len(dishes) != 40:
        errors.append("expected 40 dishes, got %d" % len(dishes))

    ids = []
    names = []
    for index, dish in enumerate(dishes):
        tag = "dish[%d]" % index
        for key in ("id", "name", "category", "description", "ingredients",
                    "steps", "nutritionPerServing"):
            if key not in dish:
                errors.append("%s missing %s" % (tag, key))

        did = dish.get("id", "")
        if not re.fullmatch(r"[a-z0-9-]+", did):
            errors.append("%s bad id %r" % (tag, did))
        ids.append(did)
        names.append(dish.get("name"))

        if dish.get("baseServings") != 2:
            errors.append("%s baseServings != 2" % tag)
        if dish.get("category") not in CATEGORIES:
            errors.append("%s bad category" % tag)
        if dish.get("difficulty") not in ("简单", "中等", "较难"):
            errors.append("%s bad difficulty" % tag)
        if not 5 <= dish.get("timeMinutes", 0) <= 240:
            errors.append("%s bad timeMinutes" % tag)
        if len(dish.get("ingredients", [])) < 2:
            errors.append("%s too few ingredients" % tag)
        for ingredient in dish.get("ingredients", []):
            if not ingredient.get("name") or not ingredient.get("unit"):
                errors.append("%s ingredient missing name/unit" % tag)
            amount = ingredient.get("amount")
            if amount is None and ingredient.get("scalable") is not False:
                errors.append("%s null amount must be non-scalable" % tag)
            if amount is not None and amount <= 0:
                errors.append("%s bad amount" % tag)
        if not 3 <= len(dish.get("steps", [])) <= 10:
            errors.append("%s bad step count" % tag)
        if not dish.get("tips"):
            errors.append("%s missing tips" % tag)
        if not dish.get("tags"):
            errors.append("%s missing tags" % tag)

        nutrition = dish.get("nutritionPerServing", {})
        if not 30 <= nutrition.get("kcal", 0) <= 900:
            errors.append("%s bad kcal" % tag)
        if not 0 <= nutrition.get("protein", 0) <= 100:
            errors.append("%s bad protein" % tag)
        if not 0 <= nutrition.get("fat", 0) <= 100:
            errors.append("%s bad fat" % tag)
        if not 0 <= nutrition.get("carbs", 0) <= 160:
            errors.append("%s bad carbs" % tag)

    if len(ids) != len(set(ids)):
        errors.append("duplicate ids")
    if len(names) != len(set(names)):
        errors.append("duplicate names")

    counts = collections.Counter(dish.get("category") for dish in dishes)
    for label, key in CATEGORIES.items():
        if counts.get(label) != 5:
            errors.append("%s category count %d != 5" % (key, counts.get(label)))

    if errors:
        print("VALIDATION FAILED")
        for error in errors:
            print("- " + error)
        return 1

    print("OK: 40 dishes, 8 categories x 5, unique ids, fields valid")
    for label, key in CATEGORIES.items():
        print("  %d x %s" % (counts[label], key))
    return 0


if __name__ == "__main__":
    sys.exit(main())
