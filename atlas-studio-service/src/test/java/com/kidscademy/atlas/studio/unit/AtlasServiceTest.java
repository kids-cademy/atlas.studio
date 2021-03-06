package com.kidscademy.atlas.studio.unit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.kidscademy.atlas.studio.AtlasService;
import com.kidscademy.atlas.studio.BusinessRules;
import com.kidscademy.atlas.studio.dao.AtlasDao;
import com.kidscademy.atlas.studio.impl.AtlasServiceImpl;
import com.kidscademy.atlas.studio.model.AtlasCollection;
import com.kidscademy.atlas.studio.model.AtlasCollectionKey;
import com.kidscademy.atlas.studio.model.AtlasItem;
import com.kidscademy.atlas.studio.model.AtlasObject;
import com.kidscademy.atlas.studio.model.ExternalSource;
import com.kidscademy.atlas.studio.model.MediaSRC;
import com.kidscademy.atlas.studio.tool.AudioProcessor;
import com.kidscademy.atlas.studio.tool.AudioSampleInfo;
import com.kidscademy.atlas.studio.tool.ImageProcessor;
import com.kidscademy.atlas.studio.util.Files;
import com.kidscademy.atlas.studio.www.CambridgeDictionary;
import com.kidscademy.atlas.studio.www.MerriamWebster;
import com.kidscademy.atlas.studio.www.SoftSchools;
import com.kidscademy.atlas.studio.www.TheFreeDictionary;

import js.json.Json;
import js.rmi.BusinessException;
import js.tiny.container.core.AppContext;
import js.tiny.container.http.form.Form;
import js.util.Classes;

@RunWith(MockitoJUnitRunner.class)
public class AtlasServiceTest {
    @Mock
    AppContext context;
    @Mock
    private AtlasDao atlasDao;
    @Mock
    private AudioProcessor audioProcessor;
    @Mock
    private ImageProcessor imageProcessor;
    @Mock
    private SoftSchools softSchools;
    @Mock
    private TheFreeDictionary freeDictionary;
    @Mock
    private CambridgeDictionary cambridgeDictionary;
    @Mock
    private MerriamWebster merriamWebster;
    @Mock
    private BusinessRules businessRules;

    private AtlasService service;

    @BeforeClass
    public static void beforeClass() {
	Classes.setFieldValue(Files.class, "REPOSITORY_DIR", "fixture/tomcat/webapps");
    }

    @Before
    public void beforeTest() throws IOException {
	when(context.getAppFile("search-index"))
		.thenReturn(new File("fixture/tomcat/work/Applications/test-app/search-index"));

	when(context.loadService(Json.class)).thenReturn(Classes.loadService(Json.class));
	when(context.getInstance(AtlasDao.class)).thenReturn(atlasDao);
	when(context.getInstance(AudioProcessor.class)).thenReturn(audioProcessor);
	when(context.getInstance(BusinessRules.class)).thenReturn(businessRules);

	service = new AtlasServiceImpl(context);

	file("sample.mp3").delete();
	file("sample_1.mp3").delete();
	file("sample_2.mp3").delete();
    }

    @Test
    public void uploadAudioSample() throws IOException {
	Form form = Mockito.mock(Form.class);
	when(form.getValue("object-id")).thenReturn("1");

	AtlasCollectionKey collection = new AtlasCollectionKey(1, "instrument");
	AtlasItem atlasItem = new AtlasItem(collection, 1, "test");
	when(atlasDao.getObjectById(AtlasItem.class, 1)).thenReturn(atlasItem);

	File uploadFile = new File("fixture/upload.mp3");
	Files.copy(new File("fixture/audio/sample.mp3"), uploadFile);
	when(form.getFile("file")).thenReturn(uploadFile);

	AudioSampleInfo info = new AudioSampleInfo();
	when(audioProcessor.getAudioFileInfo(any(File.class))).thenReturn(info);

	info = service.uploadAudioSample(form);
	assertFalse(uploadFile.exists());
	assertTrue(file("sample.mp3").exists());

	ArgumentCaptor<File> sampleFile = ArgumentCaptor.forClass(File.class);
	ArgumentCaptor<File> waveformFile = ArgumentCaptor.forClass(File.class);

	verify(audioProcessor).generateWaveform(sampleFile.capture(), waveformFile.capture());
	assertThat(sampleFile.getValue(), equalTo(file("sample.mp3")));
	assertThat(waveformFile.getValue(), equalTo(file("waveform.png")));

	assertThat(info, notNullValue());
	assertThat(info.getSampleSrc(), equalTo(src("sample.mp3")));
	assertThat(info.getWaveformSrc(), equalTo(src("waveform.png")));
    }

    @Test
    public void normalizeSample() throws IOException {
	Files.copy(new File("fixture/audio/sample.mp3"), file("sample.mp3"));

	AudioSampleInfo info = new AudioSampleInfo();
	when(audioProcessor.getAudioFileInfo(any(File.class))).thenReturn(info);

	info = service.normalizeAudioSample(atlasItem());

	ArgumentCaptor<File> audioFile = ArgumentCaptor.forClass(File.class);
	ArgumentCaptor<File> targetFile = ArgumentCaptor.forClass(File.class);
	ArgumentCaptor<File> waveformFile = ArgumentCaptor.forClass(File.class);

	verify(audioProcessor).normalizeLevel(audioFile.capture(), targetFile.capture());
	assertThat(audioFile.getValue(), equalTo(file("sample.mp3")));
	assertThat(targetFile.getValue(), equalTo(file("sample_1.mp3")));

	verify(audioProcessor).generateWaveform(targetFile.capture(), waveformFile.capture());
	assertThat(targetFile.getValue(), equalTo(file("sample.mp3")));
	assertThat(waveformFile.getValue(), equalTo(file("waveform.png")));

	assertThat(info, notNullValue());
	assertThat(info.getSampleSrc(), equalTo(src("sample.mp3")));
	assertThat(info.getWaveformSrc(), equalTo(src("waveform.png")));
    }

    @Test
    public void convertToMono() throws IOException {
	Files.copy(new File("fixture/audio/sample.mp3"), file("sample.mp3"));

	AudioSampleInfo info = new AudioSampleInfo();
	when(audioProcessor.getAudioFileInfo(any(File.class))).thenReturn(info);

	info = service.convertAudioSampleToMono(atlasItem());

	ArgumentCaptor<File> audioFile = ArgumentCaptor.forClass(File.class);
	ArgumentCaptor<File> targetFile = ArgumentCaptor.forClass(File.class);
	ArgumentCaptor<File> waveformFile = ArgumentCaptor.forClass(File.class);

	verify(audioProcessor).convertToMono(audioFile.capture(), targetFile.capture());
	assertThat(audioFile.getValue(), equalTo(file("sample.mp3")));
	assertThat(targetFile.getValue(), equalTo(file("sample_1.mp3")));

	verify(audioProcessor).generateWaveform(targetFile.capture(), waveformFile.capture());
	assertThat(targetFile.getValue(), equalTo(file("sample_1.mp3")));
	assertThat(waveformFile.getValue(), equalTo(file("waveform.png")));

	assertThat(info, notNullValue());
	assertThat(info.getSampleSrc(), equalTo(src("sample_1.mp3")));
	assertThat(info.getWaveformSrc(), equalTo(src("waveform.png")));
    }

    @Test
    public void trimSilence() throws IOException {
	Files.copy(new File("fixture/audio/sample.mp3"), file("sample.mp3"));

	AudioSampleInfo info = new AudioSampleInfo();
	when(audioProcessor.getAudioFileInfo(any(File.class))).thenReturn(info);

	info = service.trimAudioSampleSilence(atlasItem());

	ArgumentCaptor<File> audioFile = ArgumentCaptor.forClass(File.class);
	ArgumentCaptor<File> targetFile = ArgumentCaptor.forClass(File.class);
	ArgumentCaptor<File> waveformFile = ArgumentCaptor.forClass(File.class);

	verify(audioProcessor).trimSilence(audioFile.capture(), targetFile.capture());
	assertThat(audioFile.getValue(), equalTo(file("sample.mp3")));
	assertThat(targetFile.getValue(), equalTo(file("sample_1.mp3")));

	verify(audioProcessor).generateWaveform(targetFile.capture(), waveformFile.capture());
	assertThat(targetFile.getValue(), equalTo(file("sample_1.mp3")));
	assertThat(waveformFile.getValue(), equalTo(file("waveform.png")));

	assertThat(info, notNullValue());
	assertThat(info.getSampleSrc(), equalTo(src("sample_1.mp3")));
	assertThat(info.getWaveformSrc(), equalTo(src("waveform.png")));
    }

    @Test
    public void generateWaveform() throws IOException {
	Files.copy(new File("fixture/audio/sample.mp3"), file("sample.mp3"));

	MediaSRC waveformSrc = service.generateWaveform(atlasItem());

	assertThat(waveformSrc, notNullValue());
	assertThat(waveformSrc, equalTo(src("waveform.png")));
    }

    @Test
    public void undoMediaProcessing() throws IOException {
	Files.copy(new File("fixture/audio/sample.mp3"), file("sample.mp3"));
	Files.copy(new File("fixture/audio/sample.mp3"), file("sample_1.mp3"));
	Files.copy(new File("fixture/audio/sample.mp3"), file("sample_2.mp3"));

	AudioSampleInfo info = new AudioSampleInfo();
	when(audioProcessor.getAudioFileInfo(any(File.class))).thenReturn(info);

	info = service.undoAudioSampleProcessing(atlasItem());

	ArgumentCaptor<File> audioFile = ArgumentCaptor.forClass(File.class);
	ArgumentCaptor<File> waveformFile = ArgumentCaptor.forClass(File.class);

	verify(audioProcessor).generateWaveform(audioFile.capture(), waveformFile.capture());
	assertThat(audioFile.getValue(), equalTo(file("sample_1.mp3")));
	assertThat(waveformFile.getValue(), equalTo(file("waveform.png")));

	assertThat(info, notNullValue());
	assertThat(info.getSampleSrc(), equalTo(src("sample_1.mp3")));
	assertThat(info.getWaveformSrc(), equalTo(src("waveform.png")));
    }

    @Test
    public void saveObject() throws IOException, BusinessException {
	Files.copy(new File("fixture/audio/sample.mp3"), file("sample.mp3"));
	Files.copy(new File("fixture/audio/sample.mp3"), file("sample_1.mp3"));
	Files.copy(new File("fixture/audio/sample.mp3"), file("sample_2.mp3"));

	AudioSampleInfo info = new AudioSampleInfo();
	info.setSampleSrc(src("sample.mp3"));
	info.setWaveformSrc(src("waveform.png"));

	AtlasCollection collection = new AtlasCollection(1, "instrument");
	AtlasObject object = new AtlasObject(collection);
	object.setName("test");
	object.setSampleSrc(src("sample.mp3"));
	object.setSampleInfo(info);

	when(atlasDao.getAtlasObject(anyInt())).thenReturn(object);
	object = service.saveAtlasObject(object);
	info = object.getSampleInfo();

	assertTrue(file("sample.mp3").exists());
	assertFalse(file("sample_1.mp3").exists());
	assertFalse(file("sample_2.mp3").exists());

	ArgumentCaptor<File> audioFile = ArgumentCaptor.forClass(File.class);
	ArgumentCaptor<File> waveformFile = ArgumentCaptor.forClass(File.class);

	verify(audioProcessor).generateWaveform(audioFile.capture(), waveformFile.capture());
	assertThat(audioFile.getValue(), equalTo(file("sample.mp3")));
	assertThat(waveformFile.getValue(), equalTo(file("waveform.png")));

	assertThat(info, notNullValue());
	assertThat(info.getSampleSrc(), equalTo(src("sample.mp3")));
	assertThat(info.getWaveformSrc(), equalTo(src("waveform.png")));
    }

    @Test
    public void removeAudioSample() throws IOException {
	Files.copy(new File("fixture/audio/sample.mp3"), file("sample.mp3"));
	Files.copy(new File("fixture/image/waveform.png"), file("waveform.png"));

	service.removeAudioSample(atlasItem());

	assertFalse(file("sample.mp3").exists());
	assertFalse(file("waveform.png").exists());
    }

    @Test
    public void updateSearchIndex() throws NoSuchMethodException, IOException {
	service.updateIndex();
    }

    @Test
    public void getLinkDefinition_toLowerCase() throws MalformedURLException {
	URL link = new URL("https://en.wikipedia.org/wiki/Sperm_whale");
	String display = "Sperm Whale";
	ExternalSource externalSource = new ExternalSource(2, "https://en.wikipedia.org/wiki/",
		"Wikipedia article about ${display}", "definition,description,features,taxonomy");
	String definition = externalSource.getLinkDefinition(link, display);

	assertThat(definition, notNullValue());
	assertThat(definition, equalTo("Wikipedia article about Sperm Whale"));
    }

    // ----------------------------------------------------------------------------------------------

    private static AtlasItem atlasItem() {
	AtlasCollectionKey collection = new AtlasCollectionKey(1, "instrument");
	return new AtlasItem(collection, 1, "test");
    }

    private static File file(String fileName) {
	File dir = new File("fixture/tomcat/webapps/media/atlas/instrument/test/");
	dir.mkdirs();
	return new File(dir, fileName);
    }

    private static MediaSRC src(String fileName) {
	return new MediaSRC("/media/atlas/instrument/test/" + fileName);
    }
}
