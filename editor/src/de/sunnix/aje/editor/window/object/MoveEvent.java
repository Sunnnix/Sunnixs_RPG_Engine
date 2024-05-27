package de.sunnix.aje.editor.window.object;

import lombok.Getter;
import lombok.Setter;

import java.awt.event.MouseEvent;

import static de.sunnix.aje.editor.lang.Language.getString;

@Getter
@Setter
@AbstractEvent.Event(id = "move", text = "event.name.move")
public class MoveEvent extends AbstractEvent<MoveEvent> {



}
