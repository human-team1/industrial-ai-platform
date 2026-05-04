import pytest
from pydantic import ValidationError

from api.schemas.vision import RoiRequest


def test_full_frame_roi_allows_missing_coordinates():
    roi = RoiRequest(roiMode="FULL_FRAME")
    assert roi.roiMode == "FULL_FRAME"
    assert roi.roiX is None
    assert roi.roiY is None


def test_fixed_roi_within_range_passes():
    roi = RoiRequest(
        roiMode="FIXED",
        roiCoordinateType="NORMALIZED",
        roiX=0.1,
        roiY=0.2,
        roiWidth=0.5,
        roiHeight=0.4,
    )
    assert roi.roiMode == "FIXED"
    assert roi.roiX == 0.1


def test_fixed_roi_x_above_one_rejected():
    with pytest.raises(ValidationError) as exc_info:
        RoiRequest(
            roiMode="FIXED",
            roiX=1.2,
            roiY=0.0,
            roiWidth=0.5,
            roiHeight=0.5,
        )
    assert "0~1" in str(exc_info.value) or "ROI" in str(exc_info.value)


def test_fixed_roi_negative_coordinate_rejected():
    with pytest.raises(ValidationError) as exc_info:
        RoiRequest(
            roiMode="FIXED",
            roiX=-0.1,
            roiY=0.2,
            roiWidth=0.5,
            roiHeight=0.5,
        )
    assert "0~1" in str(exc_info.value) or "ROI" in str(exc_info.value)


def test_fixed_roi_missing_x_rejected():
    with pytest.raises(ValidationError) as exc_info:
        RoiRequest(
            roiMode="FIXED",
            roiX=None,
            roiY=0.2,
            roiWidth=0.3,
            roiHeight=0.4,
        )
    assert "FIXED ROI requires" in str(exc_info.value)


def test_fixed_roi_missing_all_coordinates_rejected():
    with pytest.raises(ValidationError) as exc_info:
        RoiRequest(roiMode="FIXED")
    assert "FIXED ROI requires" in str(exc_info.value)


def test_fixed_roi_x_plus_width_exceeds_one_rejected():
    with pytest.raises(ValidationError) as exc_info:
        RoiRequest(
            roiMode="FIXED",
            roiX=0.7,
            roiY=0.1,
            roiWidth=0.5,
            roiHeight=0.4,
        )
    assert "x range" in str(exc_info.value) or "0~1" in str(exc_info.value)
