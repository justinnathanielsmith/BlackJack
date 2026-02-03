package io.github.smithjustinn.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("ktlint:standard:backing-property-naming", "MagicNumber", "LongMethod", "LargeClass")
object AppIcons {
    private var _arrowBack: ImageVector? = null
    val ArrowBack: ImageVector
        get() {
            _arrowBack?.let { return it }
            return ImageVector
                .Builder(
                    name = "ArrowBack",
                    defaultWidth = 24.0.dp,
                    defaultHeight = 24.0.dp,
                    viewportWidth = 24.0f,
                    viewportHeight = 24.0f,
                ).apply {
                    path(fill = SolidColor(Color.Black)) {
                        moveTo(20.0f, 11.0f)
                        horizontalLineTo(7.83f)
                        lineTo(13.42f, 5.41f)
                        lineTo(12.0f, 4.0f)
                        lineTo(4.0f, 12.0f)
                        lineTo(12.0f, 20.0f)
                        lineTo(13.41f, 18.59f)
                        lineTo(7.83f, 13.0f)
                        horizontalLineTo(20.0f)
                        verticalLineTo(11.0f)
                        close()
                    }
                }.build()
                .also { _arrowBack = it }
        }

    private var _restart: ImageVector? = null
    val Restart: ImageVector
        get() {
            _restart?.let { return it }
            return ImageVector
                .Builder(
                    name = "Restart",
                    defaultWidth = 24.0.dp,
                    defaultHeight = 24.0.dp,
                    viewportWidth = 24.0f,
                    viewportHeight = 24.0f,
                ).apply {
                    path(fill = SolidColor(Color.Black)) {
                        moveTo(17.65f, 6.35f)
                        curveTo(16.2f, 4.9f, 14.21f, 4.0f, 12.0f, 4.0f)
                        curveToRelative(-4.42f, 0.0f, -7.99f, 3.58f, -7.99f, 8.0f)
                        reflectiveCurveToRelative(3.57f, 8.0f, 7.99f, 8.0f)
                        curveToRelative(3.73f, 0.0f, 6.84f, -2.55f, 7.73f, -6.0f)
                        horizontalLineToRelative(-2.08f)
                        curveToRelative(-0.82f, 2.33f, -3.04f, 4.0f, -5.65f, 4.0f)
                        curveToRelative(-3.31f, 0.0f, -6.0f, -2.69f, -6.0f, -6.0f)
                        reflectiveCurveToRelative(2.69f, -6.0f, 6.0f, -6.0f)
                        curveToRelative(1.66f, 0.0f, 3.14f, 0.69f, 4.22f, 1.78f)
                        lineTo(13.0f, 11.0f)
                        horizontalLineToRelative(7.0f)
                        verticalLineTo(4.0f)
                        lineToRelative(-2.35f, 2.35f)
                        close()
                    }
                }.build()
                .also { _restart = it }
        }

    private var _settings: ImageVector? = null
    val Settings: ImageVector
        get() {
            _settings?.let { return it }
            return ImageVector
                .Builder(
                    name = "Settings",
                    defaultWidth = 24.0.dp,
                    defaultHeight = 24.0.dp,
                    viewportWidth = 24.0f,
                    viewportHeight = 24.0f,
                ).path(
                    fill = SolidColor(Color.Black),
                    strokeLineWidth = 0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    pathFillType = PathFillType.NonZero,
                ) {
                    // The outer shape and teeth
                    moveTo(19.14f, 12.94f)
                    curveToRelative(0.04f, -0.3f, 0.06f, -0.61f, 0.06f, -0.94f)
                    curveToRelative(0.0f, -0.32f, -0.02f, -0.64f, -0.06f, -0.94f)
                    lineToRelative(2.03f, -1.58f)
                    curveToRelative(0.18f, -0.14f, 0.23f, -0.41f, 0.12f, -0.61f)
                    lineToRelative(-1.92f, -3.32f)
                    curveToRelative(-0.12f, -0.22f, -0.37f, -0.29f, -0.59f, -0.22f)
                    lineToRelative(-2.39f, 0.96f)
                    curveToRelative(-0.5f, -0.38f, -1.03f, -0.7f, -1.62f, -0.94f)
                    lineToRelative(-0.36f, -2.54f)
                    curveToRelative(-0.04f, -0.24f, -0.24f, -0.41f, -0.48f, -0.41f)
                    horizontalLineToRelative(-3.84f)
                    curveToRelative(-0.24f, 0.0f, -0.43f, 0.17f, -0.47f, 0.41f)
                    lineToRelative(-0.36f, 2.54f)
                    curveToRelative(-0.59f, 0.24f, -1.13f, 0.57f, -1.62f, 0.94f)
                    lineToRelative(-2.39f, -0.96f)
                    curveToRelative(-0.22f, -0.08f, -0.47f, 0.0f, -0.59f, 0.22f)
                    lineTo(2.74f, 8.87f)
                    curveToRelative(-0.12f, 0.21f, -0.08f, 0.47f, 0.12f, 0.61f)
                    lineToRelative(2.03f, 1.58f)
                    curveToRelative(-0.04f, 0.3f, -0.06f, 0.62f, -0.06f, 0.94f)
                    reflectiveCurveToRelative(0.02f, 0.64f, 0.06f, 0.94f)
                    lineToRelative(-2.03f, 1.58f)
                    curveToRelative(-0.18f, 0.14f, -0.23f, 0.41f, -0.12f, 0.61f)
                    lineToRelative(1.92f, 3.32f)
                    curveToRelative(0.12f, 0.22f, 0.37f, 0.29f, 0.59f, 0.22f)
                    lineToRelative(2.39f, -0.96f)
                    curveToRelative(0.5f, 0.38f, 1.03f, 0.7f, 1.62f, 0.94f)
                    lineToRelative(0.36f, 2.54f)
                    curveToRelative(0.05f, 0.24f, 0.24f, 0.41f, 0.48f, 0.41f)
                    horizontalLineToRelative(3.84f)
                    curveToRelative(0.24f, 0.0f, 0.44f, -0.17f, 0.47f, -0.41f)
                    lineToRelative(0.36f, -2.54f)
                    curveToRelative(0.59f, -0.24f, 1.13f, -0.56f, 1.62f, -0.94f)
                    lineToRelative(2.39f, 0.96f)
                    curveToRelative(0.22f, 0.08f, 0.47f, 0.0f, 0.59f, -0.22f)
                    lineToRelative(1.92f, -3.32f)
                    curveToRelative(0.12f, -0.21f, 0.07f, -0.47f, -0.12f, -0.61f)
                    lineToRelative(-2.01f, -1.58f)
                    close()

                    // The inner hole of the gear
                    moveTo(12f, 15.5f)
                    curveToRelative(-1.93f, 0.0f, -3.5f, -1.57f, -3.5f, -3.5f)
                    reflectiveCurveToRelative(1.57f, -3.5f, 3.5f, -3.5f)
                    reflectiveCurveToRelative(3.5f, 1.57f, 3.5f, 3.5f)
                    reflectiveCurveToRelative(-1.57f, 3.5f, -3.5f, 3.5f)
                    close()
                }.build()
                .also { _settings = it }
        }

    private var _trophy: ImageVector? = null
    val Trophy: ImageVector
        get() {
            _trophy?.let { return it }
            return ImageVector
                .Builder(
                    name = "Trophy",
                    defaultWidth = 24.0.dp,
                    defaultHeight = 24.0.dp,
                    viewportWidth = 24.0f,
                    viewportHeight = 24.0f,
                ).path(
                    fill = SolidColor(Color.Black),
                    strokeLineWidth = 0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    pathFillType = PathFillType.NonZero,
                ) {
                    // The main cup body
                    moveTo(19.0f, 5.0f)
                    horizontalLineTo(5.0f)
                    verticalLineTo(7.0f)
                    curveTo(5.0f, 10.86f, 8.13f, 14.0f, 12.0f, 14.0f)
                    reflectiveCurveTo(19.0f, 10.86f, 19.0f, 7.0f)
                    verticalLineTo(5.0f)
                    close()

                    // The stem and base
                    moveTo(11.0f, 15.0f)
                    horizontalLineTo(13.0f)
                    verticalLineTo(18.0f)
                    horizontalLineTo(16.0f)
                    verticalLineTo(20.0f)
                    horizontalLineTo(8.0f)
                    verticalLineTo(18.0f)
                    horizontalLineTo(11.0f)
                    verticalLineTo(15.0f)
                    close()

                    // Right Handle
                    moveTo(21.0f, 7.0f)
                    curveTo(22.66f, 7.0f, 24.0f, 8.34f, 24.0f, 10.0f)
                    curveTo(24.0f, 11.66f, 22.66f, 13.0f, 21.0f, 13.0f)
                    verticalLineTo(11.0f)
                    curveTo(21.55f, 11.0f, 22.0f, 10.55f, 22.0f, 10.0f)
                    curveTo(22.0f, 9.45f, 21.55f, 9.0f, 21.0f, 9.0f)
                    verticalLineTo(7.0f)
                    close()

                    // Left Handle (Mirrored)
                    moveTo(3.0f, 7.0f)
                    verticalLineTo(9.0f)
                    curveTo(2.45f, 9.0f, 2.0f, 9.45f, 2.0f, 10.0f)
                    curveTo(2.0f, 10.55f, 2.45f, 11.0f, 3.0f, 11.0f)
                    verticalLineTo(13.0f)
                    curveTo(1.34f, 13.0f, 0.0f, 11.66f, 0.0f, 10.0f)
                    curveTo(0.0f, 8.34f, 1.34f, 7.0f, 3.0f, 7.0f)
                    close()
                }.build()
                .also { _trophy = it }
        }

    private var _volumeUp: ImageVector? = null
    val VolumeUp: ImageVector
        get() {
            _volumeUp?.let { return it }
            return ImageVector
                .Builder(
                    name = "VolumeUp",
                    defaultWidth = 24.0.dp,
                    defaultHeight = 24.0.dp,
                    viewportWidth = 24.0f,
                    viewportHeight = 24.0f,
                ).apply {
                    path(fill = SolidColor(Color.Black)) {
                        moveTo(3.0f, 9.0f)
                        verticalLineToRelative(6.0f)
                        horizontalLineToRelative(4.0f)
                        lineToRelative(5.0f, 5.0f)
                        verticalLineTo(4.0f)
                        lineToRelative(-5.0f, 5.0f)
                        horizontalLineTo(3.0f)
                        close()
                        moveTo(16.5f, 12.0f)
                        curveToRelative(0.0f, -1.77f, -1.02f, -3.29f, -2.5f, -4.03f)
                        verticalLineToRelative(8.05f)
                        curveToRelative(1.48f, -0.73f, 2.5f, -2.25f, 2.5f, -4.02f)
                        close()
                        moveTo(14.0f, 3.23f)
                        verticalLineToRelative(2.06f)
                        curveToRelative(2.89f, 0.86f, 5.0f, 3.54f, 5.0f, 6.71f)
                        reflectiveCurveToRelative(-2.11f, 5.85f, -5.0f, 6.71f)
                        verticalLineToRelative(2.06f)
                        curveToRelative(4.01f, -0.91f, 7.0f, -4.49f, 7.0f, -8.77f)
                        reflectiveCurveToRelative(-2.99f, -7.86f, -7.0f, -8.77f)
                        close()
                    }
                }.build()
                .also { _volumeUp = it }
        }

    private var _volumeDown: ImageVector? = null
    val VolumeDown: ImageVector
        get() {
            _volumeDown?.let { return it }
            return ImageVector
                .Builder(
                    name = "VolumeDown",
                    defaultWidth = 24.0.dp,
                    defaultHeight = 24.0.dp,
                    viewportWidth = 24.0f,
                    viewportHeight = 24.0f,
                ).apply {
                    path(fill = SolidColor(Color.Black)) {
                        moveTo(3.0f, 9.0f)
                        verticalLineToRelative(6.0f)
                        horizontalLineToRelative(4.0f)
                        lineToRelative(5.0f, 5.0f)
                        verticalLineTo(4.0f)
                        lineToRelative(-5.0f, 5.0f)
                        horizontalLineTo(3.0f)
                        close()
                        moveTo(16.5f, 12.0f)
                        curveToRelative(0.0f, -1.77f, -1.02f, -3.29f, -2.5f, -4.03f)
                        verticalLineToRelative(8.05f)
                        curveToRelative(1.48f, -0.73f, 2.5f, -2.25f, 2.5f, -4.02f)
                        close()
                    }
                }.build()
                .also { _volumeDown = it }
        }

    private var _volumeOff: ImageVector? = null
    val VolumeOff: ImageVector
        get() {
            _volumeOff?.let { return it }
            return ImageVector
                .Builder(
                    name = "VolumeOff",
                    defaultWidth = 24.0.dp,
                    defaultHeight = 24.0.dp,
                    viewportWidth = 24.0f,
                    viewportHeight = 24.0f,
                ).apply {
                    path(fill = SolidColor(Color.Black)) {
                        moveTo(16.5f, 12.0f)
                        curveToRelative(0.0f, -1.77f, -1.02f, -3.29f, -2.5f, -4.03f)
                        verticalLineToRelative(2.21f)
                        lineToRelative(2.45f, 2.45f)
                        curveToRelative(0.03f, -0.2f, 0.05f, -0.41f, 0.05f, -0.63f)
                        close()
                        moveTo(19.0f, 12.0f)
                        curveToRelative(0.0f, 0.94f, -0.2f, 1.82f, -0.54f, 2.64f)
                        lineToRelative(1.51f, 1.51f)
                        curveTo(20.63f, 14.91f, 21.0f, 13.5f, 21.0f, 12.0f)
                        curveToRelative(0.0f, -4.28f, -2.99f, -7.86f, -7.0f, -8.77f)
                        verticalLineToRelative(2.06f)
                        curveToRelative(2.89f, 0.86f, 5.0f, 3.54f, 5.0f, 6.71f)
                        close()
                        moveTo(4.27f, 3.0f)
                        lineTo(3.0f, 4.27f)
                        lineTo(7.73f, 9.0f)
                        horizontalLineTo(3.0f)
                        verticalLineToRelative(6.0f)
                        horizontalLineToRelative(4.0f)
                        lineToRelative(5.0f, 5.0f)
                        verticalLineToRelative(-6.73f)
                        lineToRelative(4.25f, 4.25f)
                        curveToRelative(-0.67f, 0.52f, -1.42f, 0.93f, -2.25f, 1.18f)
                        verticalLineToRelative(2.06f)
                        curveToRelative(1.38f, -0.31f, 2.63f, -0.95f, 3.69f, -1.81f)
                        lineTo(19.73f, 21.0f)
                        lineTo(21.0f, 19.73f)
                        lineToRelative(-9.0f, -9.0f)
                        lineTo(4.27f, 3.0f)
                        close()
                        moveTo(12.0f, 4.0f)
                        lineTo(9.91f, 6.09f)
                        lineTo(12.0f, 8.18f)
                        verticalLineTo(4.0f)
                        close()
                    }
                }.build()
                .also { _volumeOff = it }
        }

    private var _dateRange: ImageVector? = null
    val DateRange: ImageVector
        get() {
            _dateRange?.let { return it }
            return ImageVector
                .Builder(
                    name = "DateRange",
                    defaultWidth = 24.0.dp,
                    defaultHeight = 24.0.dp,
                    viewportWidth = 24.0f,
                    viewportHeight = 24.0f,
                ).apply {
                    path(fill = SolidColor(Color.Black)) {
                        moveTo(9.0f, 11.0f)
                        horizontalLineTo(7.0f)
                        verticalLineToRelative(2.0f)
                        horizontalLineToRelative(2.0f)
                        verticalLineTo(11.0f)
                        close()
                        moveTo(13.0f, 11.0f)
                        horizontalLineToRelative(-2.0f)
                        verticalLineToRelative(2.0f)
                        horizontalLineToRelative(2.0f)
                        verticalLineTo(11.0f)
                        close()
                        moveTo(17.0f, 11.0f)
                        horizontalLineToRelative(-2.0f)
                        verticalLineToRelative(2.0f)
                        horizontalLineToRelative(2.0f)
                        verticalLineTo(11.0f)
                        close()
                        moveTo(19.0f, 4.0f)
                        horizontalLineToRelative(-1.0f)
                        verticalLineTo(2.0f)
                        horizontalLineToRelative(-2.0f)
                        verticalLineToRelative(2.0f)
                        horizontalLineTo(8.0f)
                        verticalLineTo(2.0f)
                        horizontalLineTo(6.0f)
                        verticalLineToRelative(2.0f)
                        horizontalLineTo(5.0f)
                        curveTo(3.89f, 4.0f, 3.01f, 4.9f, 3.01f, 6.0f)
                        lineTo(3.0f, 20.0f)
                        curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                        horizontalLineToRelative(14.0f)
                        curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                        verticalLineTo(6.0f)
                        curveTo(21.0f, 4.9f, 20.1f, 4.0f, 19.0f, 4.0f)
                        close()
                        moveTo(19.0f, 20.0f)
                        horizontalLineTo(5.0f)
                        verticalLineTo(9.0f)
                        horizontalLineToRelative(14.0f)
                        verticalLineTo(20.0f)
                        close()
                    }
                }.build()
                .also { _dateRange = it }
        }

    private var _share: ImageVector? = null
    val Share: ImageVector
        get() {
            _share?.let { return it }
            return ImageVector
                .Builder(
                    name = "Share",
                    defaultWidth = 24.0.dp,
                    defaultHeight = 24.0.dp,
                    viewportWidth = 24.0f,
                    viewportHeight = 24.0f,
                ).apply {
                    path(fill = SolidColor(Color.Black)) {
                        moveTo(18.0f, 16.08f)
                        curveToRelative(-0.76f, 0.0f, -1.44f, 0.3f, -1.96f, 0.77f)
                        lineTo(8.91f, 12.7f)
                        curveToRelative(0.05f, -0.23f, 0.09f, -0.46f, 0.09f, -0.7f)
                        reflectiveCurveToRelative(-0.04f, -0.47f, -0.09f, -0.7f)
                        lineToRelative(7.05f, -4.11f)
                        curveToRelative(0.54f, 0.5f, 1.25f, 0.81f, 2.04f, 0.81f)
                        curveToRelative(1.66f, 0.0f, 3.0f, -1.34f, 3.0f, -3.0f)
                        reflectiveCurveToRelative(-1.34f, -3.0f, -3.0f, -3.0f)
                        reflectiveCurveToRelative(-3.0f, 1.34f, -3.0f, 3.0f)
                        curveToRelative(0.0f, 0.24f, 0.04f, 0.47f, 0.09f, 0.7f)
                        lineTo(8.04f, 9.81f)
                        curveTo(7.5f, 9.31f, 6.79f, 9.0f, 6.0f, 9.0f)
                        curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                        reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
                        curveToRelative(0.79f, 0.0f, 1.5f, -0.31f, 2.04f, -0.81f)
                        lineToRelative(7.12f, 4.16f)
                        curveToRelative(-0.05f, 0.21f, -0.08f, 0.43f, -0.08f, 0.65f)
                        curveToRelative(0.0f, 1.61f, 1.31f, 2.92f, 2.92f, 2.92f)
                        reflectiveCurveToRelative(2.92f, -1.31f, 2.92f, -2.92f)
                        reflectiveCurveToRelative(-1.31f, -2.92f, -2.92f, -2.92f)
                        close()
                    }
                }.build()
                .also { _share = it }
        }

    private var _visibilityOff: ImageVector? = null
    val VisibilityOff: ImageVector
        get() {
            _visibilityOff?.let { return it }
            return ImageVector
                .Builder(
                    name = "VisibilityOff",
                    defaultWidth = 24.0.dp,
                    defaultHeight = 24.0.dp,
                    viewportWidth = 24.0f,
                    viewportHeight = 24.0f,
                ).apply {
                    path(fill = SolidColor(Color.Black)) {
                        moveTo(12.0f, 7.0f)
                        curveToRelative(2.76f, 0.0f, 5.0f, 2.24f, 5.0f, 5.0f)
                        curveToRelative(0.0f, 0.65f, -0.13f, 1.26f, -0.36f, 1.81f)
                        lineToRelative(2.42f, 2.42f)
                        curveTo(20.44f, 14.96f, 21.37f, 13.59f, 22.0f, 12.0f)
                        curveToRelative(-1.73f, -4.39f, -6.0f, -7.5f, -11.0f, -7.5f)
                        curveToRelative(-1.27f, 0.0f, -2.48f, 0.2f, -3.61f, 0.57f)
                        lineToRelative(2.13f, 2.13f)
                        curveTo(10.3f, 7.07f, 11.12f, 7.0f, 12.0f, 7.0f)
                        close()
                        moveTo(2.0f, 4.27f)
                        lineToRelative(2.28f, 2.28f)
                        lineToRelative(0.46f, 0.46f)
                        curveTo(3.26f, 8.44f, 2.13f, 10.09f, 1.11f, 12.0f)
                        curveToRelative(1.73f, 4.39f, 6.0f, 7.5f, 11.0f, 7.5f)
                        curveToRelative(1.55f, 0.0f, 3.03f, -0.3f, 4.38f, -0.84f)
                        lineToRelative(0.42f, 0.42f)
                        lineTo(19.73f, 22.0f)
                        lineTo(21.0f, 20.73f)
                        lineTo(3.27f, 3.0f)
                        lineTo(2.0f, 4.27f)
                        close()
                        moveTo(7.53f, 9.8f)
                        lineToRelative(1.55f, 1.55f)
                        curveTo(9.03f, 11.49f, 9.0f, 11.74f, 9.0f, 12.0f)
                        curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                        curveToRelative(0.26f, 0.0f, 0.51f, -0.03f, 0.65f, -0.08f)
                        lineToRelative(1.55f, 1.55f)
                        curveToRelative(-0.67f, 0.33f, -1.41f, 0.53f, -2.2f, 0.53f)
                        curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
                        curveToRelative(0.0f, -0.79f, 0.2f, -1.53f, 0.53f, -2.2f)
                        close()
                        moveTo(11.84f, 9.02f)
                        lineToRelative(3.15f, 3.15f)
                        lineToRelative(0.02f, -0.16f)
                        curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                        lineToRelative(-0.17f, 0.01f)
                        close()
                    }
                }.build()
                .also { _visibilityOff = it }
        }

    private var _swapHoriz: ImageVector? = null
    val SwapHoriz: ImageVector
        get() {
            _swapHoriz?.let { return it }
            return ImageVector
                .Builder(
                    name = "SwapHoriz",
                    defaultWidth = 24.0.dp,
                    defaultHeight = 24.0.dp,
                    viewportWidth = 24.0f,
                    viewportHeight = 24.0f,
                ).apply {
                    path(fill = SolidColor(Color.Black)) {
                        moveTo(6.99f, 11.0f)
                        lineTo(3.0f, 15.0f)
                        lineToRelative(3.99f, 4.0f)
                        verticalLineToRelative(-3.0f)
                        horizontalLineTo(14.0f)
                        verticalLineToRelative(-2.0f)
                        horizontalLineTo(6.99f)
                        verticalLineToRelative(-3.0f)
                        close()
                        moveTo(21.0f, 9.0f)
                        lineToRelative(-3.99f, -4.0f)
                        verticalLineToRelative(3.0f)
                        horizontalLineTo(10.0f)
                        verticalLineToRelative(2.0f)
                        horizontalLineToRelative(7.01f)
                        verticalLineToRelative(3.0f)
                        lineTo(21.0f, 9.0f)
                        close()
                    }
                }.build()
                .also { _swapHoriz = it }
        }
}

object ShopIcons {
    private var _casinoChip: ImageVector? = null
    val CasinoChip: ImageVector
        get() {
            _casinoChip?.let { return it }
            return ImageVector
                .Builder(
                    name = "CasinoChip",
                    defaultWidth = 24.0.dp,
                    defaultHeight = 24.0.dp,
                    viewportWidth = 24.0f,
                    viewportHeight = 24.0f,
                ).apply {
                    // 1. The Outer Ring (Rim)
                    path(fill = SolidColor(Color.Black)) {
                        moveTo(12.0f, 2.0f)
                        curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                        reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                        reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                        reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                        close()
                        // Create the "Ring" effect by cutting out the inner circle
                        moveTo(12.0f, 20.0f)
                        curveToRelative(-4.41f, 0.0f, -8.0f, -3.59f, -8.0f, -8.0f)
                        reflectiveCurveToRelative(3.59f, -8.0f, 8.0f, -8.0f)
                        reflectiveCurveToRelative(8.0f, 3.59f, 8.0f, 8.0f)
                        reflectiveCurveToRelative(-3.59f, 8.0f, -8.0f, 8.0f)
                        close()
                    }

                    // 2. The 4 Cardinal "Spots" (Classic Poker Chip Detail)
                    path(fill = SolidColor(Color.Black)) {
                        // Top Spot
                        moveTo(11.0f, 2.0f)
                        horizontalLineToRelative(2.0f)
                        verticalLineToRelative(4.0f)
                        horizontalLineToRelative(-2.0f)
                        close()
                        // Bottom Spot
                        moveTo(11.0f, 18.0f)
                        horizontalLineToRelative(2.0f)
                        verticalLineToRelative(4.0f)
                        horizontalLineToRelative(-2.0f)
                        close()
                        // Left Spot
                        moveTo(2.0f, 11.0f)
                        horizontalLineToRelative(4.0f)
                        verticalLineToRelative(2.0f)
                        horizontalLineTo(2.0f)
                        close()
                        // Right Spot
                        moveTo(18.0f, 11.0f)
                        horizontalLineToRelative(4.0f)
                        verticalLineToRelative(2.0f)
                        horizontalLineToRelative(-4.0f)
                        close()
                    }

                    // 3. The Inner Face
                    path(fill = SolidColor(Color.Black)) {
                        moveTo(12.0f, 7.0f)
                        curveToRelative(-2.76f, 0.0f, -5.0f, 2.24f, -5.0f, 5.0f)
                        reflectiveCurveToRelative(2.24f, 5.0f, 5.0f, 5.0f)
                        reflectiveCurveToRelative(5.0f, -2.24f, 5.0f, -5.0f)
                        reflectiveCurveToRelative(-2.24f, -5.0f, -5.0f, -5.0f)
                        close()
                    }
                }.build()
                .also { _casinoChip = it }
        }

    private var _shoppingCart: ImageVector? = null
    val ShoppingCart: ImageVector
        get() {
            _shoppingCart?.let { return it }
            return ImageVector
                .Builder(
                    name = "ShoppingCart",
                    defaultWidth = 24.0.dp,
                    defaultHeight = 24.0.dp,
                    viewportWidth = 24.0f,
                    viewportHeight = 24.0f,
                ).apply {
                    path(fill = SolidColor(Color.Black)) {
                        moveTo(7.0f, 18.0f)
                        curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                        reflectiveCurveTo(5.9f, 22.0f, 7.0f, 22.0f)
                        reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                        reflectiveCurveToRelative(-0.9f, -2.0f, -1.0f, -2.0f)
                        close()
                        moveTo(1.0f, 2.0f)
                        verticalLineToRelative(2.0f)
                        horizontalLineToRelative(2.0f)
                        lineToRelative(3.6f, 7.59f)
                        lineToRelative(-1.35f, 2.45f)
                        curveToRelative(-0.16f, 0.28f, -0.25f, 0.61f, -0.25f, 0.96f)
                        curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                        horizontalLineToRelative(12.0f)
                        verticalLineToRelative(-2.0f)
                        horizontalLineTo(7.42f)
                        curveToRelative(-0.14f, 0.0f, -0.25f, -0.11f, -0.25f, -0.25f)
                        lineToRelative(0.03f, -0.12f)
                        lineToRelative(0.9f, -1.63f)
                        horizontalLineToRelative(7.45f)
                        curveToRelative(0.75f, 0.0f, 1.41f, -0.41f, 1.75f, -1.03f)
                        lineToRelative(3.58f, -6.49f)
                        curveToRelative(0.08f, -0.14f, 0.12f, -0.31f, 0.12f, -0.48f)
                        curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                        horizontalLineTo(4.21f)
                        lineTo(3.52f, 2.0f)
                        horizontalLineTo(1.0f)
                        close()
                        moveTo(17.0f, 18.0f)
                        curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                        reflectiveCurveToRelative(0.9f, 2.0f, 1.0f, 2.0f)
                        reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                        reflectiveCurveToRelative(-0.9f, -2.0f, -1.0f, -2.0f)
                        close()
                    }
                }.build()
                .also { _shoppingCart = it }
        }

    private var _checkCircle: ImageVector? = null
    val CheckCircle: ImageVector
        get() {
            _checkCircle?.let { return it }
            return ImageVector
                .Builder(
                    name = "CheckCircle",
                    defaultWidth = 24.0.dp,
                    defaultHeight = 24.0.dp,
                    viewportWidth = 24.0f,
                    viewportHeight = 24.0f,
                ).apply {
                    path(fill = SolidColor(Color.Black)) {
                        moveTo(12.0f, 2.0f)
                        curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                        reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                        reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                        reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                        close()
                        // Inner checkmark
                        moveTo(10.0f, 17.0f)
                        lineToRelative(-5.0f, -5.0f)
                        lineToRelative(1.41f, -1.41f)
                        lineTo(10.0f, 14.17f)
                        lineToRelative(7.59f, -7.59f)
                        lineTo(19.0f, 8.0f)
                        lineToRelative(-9.0f, 9.0f)
                        close()
                    }
                }.build()
                .also { _checkCircle = it }
        }
}
