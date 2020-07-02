package com.kidscademy.atlas.studio.export;

import java.util.HashMap;
import java.util.Map;

import com.kidscademy.atlas.studio.model.Feature;
import com.kidscademy.atlas.studio.model.FeatureValueFormat;
import com.kidscademy.atlas.studio.model.PhysicalQuantity;

import js.lang.BugError;
import js.util.Strings;

@SuppressWarnings("unused")
public class ExportFeature {
    private final String name;
    private final double value;
    private final Double maximum;
    private final PhysicalQuantity quantity;

    private final String nameDisplay;
    private final String valueDisplay;

    public ExportFeature(Feature feature) {
	this.name = feature.getName();
	this.value = feature.getValue();
	this.maximum = feature.getMaximum();
	this.quantity = feature.getQuantity();

	this.nameDisplay = nameDisplay(feature.getName());
	this.valueDisplay = new FeatureValueFormat(feature).display();
    }

    @Override
    public String toString() {
	return Strings.toString(name, value, maximum, quantity);
    }

    public static String nameDisplay(String name) {
	String display = EN.get(name);
	if (display == null) {
	    throw new BugError("Not mapped feature name |%s|.", name);
	}
	return display;
    }

    private static final Map<String, String> EN = new HashMap<>();
    static {
	EN.put("lifespan", "Lifespan");
	EN.put("lifespan.record", "Lifespan Record");
	EN.put("lifespan.captivity", "Lifespan in Captivity");
	EN.put("length", "Body Length");
	EN.put("length.bill", "Bill Length");
	EN.put("length.tail", "Tail Length");
	EN.put("length.claw", "Claws Size");
	EN.put("height", "Height");
	EN.put("height.shoulder", "Height at Shoulder");
	EN.put("height.hip", "Height at Hip");
	EN.put("wingspan", "Wingspan");
	EN.put("weight", "Weight");
	EN.put("speed", "Speed");
	EN.put("speed.full", "Speed Record");
	EN.put("speed.record", "Full Speed");
	EN.put("speed.gliding", "Gliding Speed");
	EN.put("speed.diving", "Diving Speed");
	EN.put("flying.height", "Flying Height");
	EN.put("elevation", "Elevation");
	EN.put("depth", "Diving Depth");
	EN.put("nutrient.energy", "Food Energy");
	EN.put("nutrient.calories", "Food Energy");
	EN.put("nutrient.protein", "Protein");
	EN.put("nutrient.fat", "Total Fat");
	EN.put("nutrient.sugars", "Sugars");
	EN.put("nutrient.dietary.fiber", "Dietary Fiber");
	EN.put("nutrient.starch", "Starch");
	EN.put("nutrient.ethanol", "Drinking Alcohol");
	EN.put("nutrient.carbohydrates", "Carbohydrates");
	EN.put("nutrient.water", "Water");
	EN.put("orbit.radius", "Mean Orbit Radius");
	EN.put("orbit.eccentricity", "Orbital Eccentricity");
	EN.put("orbit.period", "Orbital Period");
	EN.put("orbit.speed", "Orbital Speed");
	EN.put("orbit.aphelion", "Orbit Aphelion");
	EN.put("orbit.perihelion", "Orbit Perihelion");
	EN.put("width", "Width");
	EN.put("crew", "Crew");
	EN.put("range", "Range");
	EN.put("range.maximum", "Maximum Range");
	EN.put("engine.power", "Engine Power");
	EN.put("ship.beam", "Ship Beam");
	EN.put("ship.beam.maximum", "Maximum Ship Beam");
	EN.put("ship.draft", "Ship Draft");
	EN.put("ship.displacement", "Ship Displacement");
	EN.put("radius", "Mean Radius");
	EN.put("mass", "Mass");
	EN.put("albedo", "Albedo");
	EN.put("density", "Mean Density");
	EN.put("speed.escape", "Escape velocity");
	EN.put("gravity", "Surface Gravity");
	EN.put("rotation.period", "Rotation Period");
	EN.put("axial.tilt", "Axial Tilt");
	EN.put("population.urban", "Urban Population");
	EN.put("population.metro", "Metro Population");
	EN.put("population.density", "Population Density");
	EN.put("area.urban", "Urban Area");
	EN.put("area.metro", "Metro Area");
	EN.put("gva.total", "Total GVA");
	EN.put("gva.percapita", "GVA per capita");
    }
}
