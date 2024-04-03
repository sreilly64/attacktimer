package com.attacktimer;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import javax.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Font;
import java.util.Vector;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPriority;

@Slf4j
public class AttackTimerMetronomeTileOverlay extends Overlay
{

    private final Client client;
    private final AttackTimerMetronomeConfig config;
    private final AttackTimerMetronomePlugin plugin;

    @Inject
    public AttackTimerMetronomeTileOverlay(Client client, AttackTimerMetronomeConfig config, AttackTimerMetronomePlugin plugin)
    {
        super(plugin);
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        setPriority(OverlayPriority.MED);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (plugin.attackState != AttackTimerMetronomePlugin.AttackState.DELAYED) {
            return null;
        }

        if (config.showTick())
        {
            if (config.fontType() == FontTypes.REGULAR)
            {
                graphics.setFont(new Font(FontManager.getRunescapeFont().getName(), Font.PLAIN, config.fontSize()));
            }
            else
            {
                graphics.setFont(new Font(config.fontType().toString(), Font.PLAIN, config.fontSize()));
            }

            Integer cameraX = client.getCameraX();
            Integer cameraY = client.getCameraY();
            Integer playerX = client.getLocalPlayer().getLocalLocation().getX();
            Integer playerY = client.getLocalPlayer().getLocalLocation().getY();

            //90 degree counterclockwise rotation matrix equations, finds the point to the right of character model relative to the camera POV
            Integer overlayRelativeX = (playerX + playerY) - cameraY;
            Integer overlayRelativeY = cameraX + (playerY - playerX);

            // d=√((x2 – x1)² + (y2 – y1)²) ; Equation to find distance from camera (or above point) to the character model
            Integer horizontalDistanceFromPlayerToOverlay = 50; //TODO make this a configuration
            Double distanceToCamera = Math.sqrt(Math.pow((playerX - cameraX), 2) + Math.pow((playerY - cameraY), 2));

            //ratio that will be applied to below equation in order to determine X and Y position of the overlay so that it is always a fixed distance from the character model
            Double t = horizontalDistanceFromPlayerToOverlay / distanceToCamera;

            //(xt,yt)=(((1−t)*x0 + t*x1),((1−t)*y0 + t*y1)) ; Equation to find a point in the direction of (overlayRelativeX, overlayRelativeY) with specified distance from character model
            Double displayX = ((1-t) * playerX) + (t * overlayRelativeX);
            Double displayY = ((1-t) * playerY) + (t * overlayRelativeY);
            LocalPoint displayLocalPoint = new LocalPoint((int) Math.round(displayX), (int) Math.round(displayY));

            final int height = client.getLocalPlayer().getLogicalHeight()+20; //TODO make this a configuration
            final Point playerPoint = Perspective.localToCanvas(client, displayLocalPoint, client.getPlane(), height);
            // Countdown ticks instead of up.
            // plugin.tickCounter => ticksRemaining
            int ticksRemaining = plugin.getTicksUntilNextAttack();
            OverlayUtil.renderTextLocation(graphics, playerPoint, String.valueOf(ticksRemaining), config.NumberColor());
        }

        return null;
    }

    private void renderTile(final Graphics2D graphics, final LocalPoint dest, final Color color, final Color fillColor, final double borderWidth)
    {
        if (dest == null)
        {
            return;
        }

        final Polygon poly = Perspective.getCanvasTilePoly(client, dest);

        if (poly == null)
        {
            return;
        }

        OverlayUtil.renderPolygon(graphics, poly, color, fillColor, new BasicStroke((float) borderWidth));
    }
}


