// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.history;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentSkipListSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.streetside.StreetsideAbstractImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideImage;
import org.openstreetmap.josm.plugins.streetside.StreetsideLayer;
import org.openstreetmap.josm.plugins.streetside.history.commands.CommandJoin;
import org.openstreetmap.josm.plugins.streetside.history.commands.CommandMove;
import org.openstreetmap.josm.plugins.streetside.history.commands.CommandTurn;
import org.openstreetmap.josm.plugins.streetside.history.commands.CommandUnjoin;
import org.openstreetmap.josm.plugins.streetside.history.commands.StreetsideCommand;
import org.openstreetmap.josm.testutils.annotations.Main;
import org.openstreetmap.josm.testutils.annotations.Projection;

/**
 * Tests the command record system.
 *
 * @author nokutu
 */
@Main
@Projection
@Disabled
class StreetsideRecordTest {

  private StreetsideRecord record;
  private StreetsideImage img1;
  private StreetsideImage img2;
  private StreetsideImage img3;

  /**
   * Creates a new {@link StreetsideRecord} object and 3 {@link StreetsideImage}
   * objects.
   */
  @BeforeEach
  public void setUp() {
    record = new StreetsideRecord();
    img1 = new StreetsideImage("key1__________________", new LatLon(0.1, 0.1), 0.1);
    img2 = new StreetsideImage("key2__________________", new LatLon(0.2, 0.2), 0.2);
    img3 = new StreetsideImage("key3__________________", new LatLon(0.3, 0.3), 0.3);
    if (StreetsideLayer.hasInstance() && StreetsideLayer.getInstance().getData().getImages().size() >= 1) {
      StreetsideLayer.getInstance().getData().getImages().clear();
    }
  }

  /**
   * Test commands in general.
   */
  @Test
  void testCommand() {
    StreetsideCommand cmd12 = new CommandMove(
            new ConcurrentSkipListSet<>(Arrays.asList(img1, img2)),
            0.1, 0.1);
    StreetsideCommand cmd23 = new CommandMove(
            new ConcurrentSkipListSet<>(Arrays.asList(img2, img3)),
            0.1, 0.1);
    StreetsideCommand cmd13 = new CommandMove(
            new ConcurrentSkipListSet<>(Arrays.asList(img1, img3)),
            0.1, 0.1);
    StreetsideCommand cmd1 = new CommandMove(
            new ConcurrentSkipListSet<>(Collections.singletonList(img1)), 0.1, 0.1);
    StreetsideCommand cmd31 = new CommandMove(
            new ConcurrentSkipListSet<>(Arrays.asList(img3, img1)),
            0.2, 0.2);
    record.addCommand(cmd12);
    record.addCommand(cmd23);

    assertEquals(1, record.position);
    assertEquals(2, record.commandList.size());

    record.undo();

    assertEquals(0, record.position);
    assertEquals(2, record.commandList.size());

    record.addCommand(cmd1);

    assertEquals(1, record.position);

    record.addCommand(cmd13);

    assertEquals(2, record.position);
    assertEquals(3, record.commandList.size());

    record.undo();
    record.redo();

    assertEquals(2, record.position);
    assertEquals(3, record.commandList.size());

    record.addCommand(cmd31);

    assertEquals(2, record.position);
    assertEquals(3, record.commandList.size());

    record.addCommand(cmd1);

    assertEquals(3, record.position);
    assertEquals(4, record.commandList.size());
  }

  /**
   * Tests {@link CommandMove} class.
   */
  @Test
  void testCommandMove() {
    CommandMove cmd1 = new CommandMove(
            new ConcurrentSkipListSet<>(Arrays.asList(img1, img2)),
            0.1, 0.1);
    CommandMove cmd2 = new CommandMove(
            new ConcurrentSkipListSet<>(Arrays.asList(img1, img2)),
            0.1, 0.1);

    record.addCommand(cmd1);

    assertEquals(0.1, img1.getMovingLatLon().lat(), 0.01);

    record.undo();

    assertEquals(0.0, img1.getMovingLatLon().lat(), 0.01);

    record.redo();

    assertEquals(0.1, img1.getMovingLatLon().lat(), 0.01);

    record.addCommand(cmd2);
    record.undo();

    assertEquals(-0.1, img1.getMovingLatLon().lat(), 0.01);

    record.redo();

    assertEquals(0.1, img1.getMovingLatLon().lat(), 0.01);
  }

  /**
   * Tests {@link CommandTurn} class.
   */
  @Test
  void testCommandTurn() {
    CommandTurn cmd1 = new CommandTurn(
            new ConcurrentSkipListSet<>(Arrays.asList(img1, img2)),
            0.2);
    CommandTurn cmd2 = new CommandTurn(
            new ConcurrentSkipListSet<>(Arrays.asList(img1, img2)),
            0.1);

    record.addCommand(cmd1);
    record.undo();

    assertEquals(-0.1, img1.getMovingHe(), 0.01);

    record.redo();

    assertEquals(0.1, img1.getMovingHe(), 0.01);

    record.addCommand(cmd2);
    record.undo();

    assertEquals(-0.2, img1.getMovingHe(), 0.01);

    record.redo();

    assertEquals(0.1, img1.getMovingHe(), 0.01);
  }

  /**
   * Tests {@link CommandJoin} class.
   */
  @Test
  void testCommandJoinClass() {
    CommandJoin cmd1 = new CommandJoin(img1, img2);
    CommandJoin cmd2 = new CommandJoin(img2, img3);

    record.addCommand(cmd1);
    assertEquals(2, img1.getSequence().getImages().size());
    assertEquals(img2, img1.next());
    record.undo();
    assertEquals(1, img1.getSequence().getImages().size());
    record.redo();
    record.addCommand(cmd2);
    assertEquals(3, img1.getSequence().getImages().size());
    assertEquals(img3, img1.next().next());
  }

  @Test
  void testCommandJoinNull1() {
    assertThrows(NullPointerException.class, () -> new CommandJoin(img1, null));
  }

  @Test
  void commandJoinNull2() {
    assertThrows(NullPointerException.class, () -> new CommandJoin(null, img1));
  }

  /**
   * Tests {@link CommandUnjoin} class.
   */
  @Test
  void testCommandUnjoinClass() {
    CommandJoin join1 = new CommandJoin(img1, img2);
    CommandJoin join2 = new CommandJoin(img2, img3);

    CommandUnjoin cmd1 = new CommandUnjoin(
            Arrays.asList(new StreetsideAbstractImage[]{img1, img2}));
    CommandUnjoin cmd2 = new CommandUnjoin(
            Arrays.asList(new StreetsideAbstractImage[]{img2, img3}));

    record.addCommand(join1);
    record.addCommand(join2);

    record.addCommand(cmd1);
    assertEquals(1, img1.getSequence().getImages().size());
    record.undo();
    assertEquals(3, img1.getSequence().getImages().size());
    record.redo();
    record.addCommand(cmd2);
    assertEquals(1, img1.getSequence().getImages().size());
    assertEquals(1, img2.getSequence().getImages().size());

    CommandUnjoin command = new CommandUnjoin(Arrays.asList(new StreetsideAbstractImage[]{img1, img2, img3}));
    assertThrows(IllegalArgumentException.class, () -> record.addCommand(command));
  }
}
