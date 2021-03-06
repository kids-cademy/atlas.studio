package com.kidscademy.atlas.studio.unit;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.kidscademy.atlas.studio.model.AtlasObject;
import com.kidscademy.atlas.studio.model.ExternalSource;
import com.kidscademy.atlas.studio.model.Fact;
import com.kidscademy.atlas.studio.model.HDate;
import com.kidscademy.atlas.studio.model.Link;
import com.kidscademy.atlas.studio.model.LinkSource;
import com.kidscademy.atlas.studio.model.Region;

import js.json.Json;
import js.util.Classes;

public class AtlasObjectTest
{
  @Test
  public void serializeJSON() throws MalformedURLException {
    AtlasObject object = new AtlasObject();
    object.setId(1);
    object.setName("banjo");
    object.setDisplay("Banjo");
    object.setDescription("Banjo description.");
    object.setSampleTitle("Banjo solo");
    object.setSampleName("sample.mp3");
    object.setStartDate(new HDate(1821, HDate.Format.YEAR, HDate.Period.MIDDLE));

    // TODO: add pictures

    List<String> aliases = new ArrayList<String>();
    aliases.add("Banjo Alias #1");
    aliases.add("Banjo Alias #2");
    object.setAliases(aliases);

    List<Region> spreading = new ArrayList<Region>();
    spreading.add(new Region("Europe", Region.Area.SOUTH));
    spreading.add(new Region("Africa", Region.Area.NORTH));
    object.setSpreading(spreading);

    ExternalSource externalSource = new ExternalSource(1, "https://en.wikipedia.org/wiki/", "Wikipedia article about ${display}", "definition,description,features,taxonomy");
    LinkSource linkSource = new LinkSource(1, externalSource);
    List<Link> links = new ArrayList<>();
    links.add(new Link(linkSource, new URL("https://en.wikipedia.org/wiki/AccordionXXX"), object.getDisplay()));
    links.add(new Link(linkSource, new URL("http://en.wikipedia.org:443/wiki/Accordion"), object.getDisplay()));
    object.setLinks(links);

    List<Fact> facts = new ArrayList<>();
    facts.add(new Fact("Banjo Fact #1", "Banjo fact #1 description."));
    facts.add(new Fact("Banjo Fact #2", "Banjo fact #2 description."));
    object.setFacts(facts);

    Json json = Classes.loadService(Json.class);
    String value = json.stringify(object);
    System.out.println(value);
  }

  @Test
  public void deserializeJSON() throws Exception {
    Json json = Classes.loadService(Json.class);
    AtlasObject object = json.parse(Classes.getResourceAsReader("instrument.json"), AtlasObject.class);
    System.out.println(object);
  }
}
