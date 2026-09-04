$(document).ready(function () {

	$("#uploadExcel").click(function () {

	    uploadExcel();

	});
	
    $("#loadSuggestions").click(function () {

        const sourceProfileId =
            $("#sourceProfileId").val();

        const templateVersionId =
            $("#templateVersionId").val();

        if (!sourceProfileId || !templateVersionId) {
            showMessage(
                "Please enter Source Profile ID and Template Version ID.",
                "error"
            );
            return;
        }

        loadSuggestions(
            sourceProfileId,
            templateVersionId
        );
    });

});


function loadSuggestions(
    sourceProfileId,
    templateVersionId
) {

    $("#mappingTable tbody").empty();

    showMessage(
        "Loading mapping suggestions...",
        "info"
    );

    $.ajax({

        url: "/api/mapping-suggestions",

        method: "GET",

        data: {
            sourceProfileId: sourceProfileId,
            templateVersionId: templateVersionId
        },

        success: function (suggestions) {

            $("#mappingTable tbody").empty();

            if (suggestions.length === 0) {

                showMessage(
                    "No mapping suggestions found.",
                    "info"
                );

                return;
            }

            suggestions.forEach(function (suggestion) {

                addSuggestionRow(suggestion);

            });

            showMessage(
                suggestions.length +
                " mapping suggestions loaded.",
                "success"
            );
        },

        error: function (xhr) {

            console.error(xhr);

            showMessage(
                "Failed to load mapping suggestions.",
                "error"
            );
        }
    });
}


function addSuggestionRow(suggestion) {

    const row = $("<tr>");

    row.append(
        $("<td>").text(
            suggestion.sourceColumnName
        )
    );

    row.append(
        $("<td>").text(
            suggestion.sourceDataType
        )
    );

    row.append(
        $("<td>").text(
            suggestion.targetFieldName
        )
    );

    row.append(
        $("<td>").text(
            suggestion.targetDataType
        )
    );

    const confidenceCell = $("<td>");

    const confidence = $("<span>")
        .addClass("confidence")
        .text(suggestion.confidence + "%");

    if (suggestion.confidence >= 90) {
        confidence.addClass("high-confidence");
    } else if (suggestion.confidence >= 70) {
        confidence.addClass("medium-confidence");
    } else {
        confidence.addClass("low-confidence");
    }

    confidenceCell.append(confidence);

    row.append(confidenceCell);


    const actionCell = $("<td>");

    const acceptButton =
        $("<button>")
            .addClass("accept-button")
            .text("Accept");

    acceptButton.click(function () {

        acceptMapping(
            suggestion,
            row
        );

    });

    actionCell.append(acceptButton);

    row.append(actionCell);

    $("#mappingTable tbody").append(row);
}


function acceptMapping(
    suggestion,
    row
) {

    const request = {

        sourceColumnId:
            suggestion.sourceColumnId,

        targetFieldId:
            suggestion.targetFieldId,

        transformation:
            "DIRECT",

        mappingMethod:
            "AI",

        aiConfidence:
            suggestion.confidence,

        aiRecommendedTargetFieldId:
            suggestion.targetFieldId
    };


    $.ajax({

        url: "/api/mappings",

        method: "POST",

        contentType: "application/json",

        data: JSON.stringify(request),

        success: function () {

            row.addClass("accepted");

            row.find(".accept-button")
                .prop("disabled", true)
                .text("Accepted");

            showMessage(
                suggestion.sourceColumnName +
                " → " +
                suggestion.targetFieldName +
                " saved.",
                "success"
            );
        },

        error: function (xhr) {

            console.error(xhr);

            let message =
                "Could not save mapping.";

            if (xhr.responseText) {
                message += " " + xhr.responseText;
            }

            showMessage(
                message,
                "error"
            );
        }
    });
}


function showMessage(
    message,
    type
) {

    const messageBox =
        $("#message");

    messageBox
        .removeClass(
            "success error info"
        )
        .addClass(type)
        .text(message);
}

let uploadedFile = null;

function uploadExcel() {

    const fileInput =
        $("#excelFile")[0];

    if (fileInput.files.length === 0) {

        showUploadMessage(
            "Please select an Excel file.",
            "error"
        );

        return;
    }


    uploadedFile =
        fileInput.files[0];


    const formData =
        new FormData();

    formData.append(
        "file",
        uploadedFile
    );


    showUploadMessage(
        "Uploading Excel file...",
        "info"
    );


    $.ajax({

        url: "/api/excel/upload",

        method: "POST",

        data: formData,

        processData: false,

        contentType: false,

        success: function (sheetNames) {

            console.log(
                "Sheet names:",
                sheetNames
            );


            if (!sheetNames || sheetNames.length === 0) {

                showUploadMessage(
                    "No worksheet found in the Excel file.",
                    "error"
                );

                return;
            }


            const sheetName =
                sheetNames[0];


            console.log(
                "Using worksheet:",
                sheetName
            );


            profileSheet(
                uploadedFile,
                sheetName
            );
        },

        error: function (xhr) {

            console.error(xhr);

            showUploadMessage(
                "Failed to read Excel file.",
                "error"
            );
        }

    });
}

function profileSheet(
    file,
    sheetName
) {

    const formData =
        new FormData();

    formData.append(
        "file",
        file
    );

    formData.append(
        "sheet",
        sheetName
    );


    showUploadMessage(
        "Profiling worksheet...",
        "info"
    );


    $.ajax({

        url: "/api/excel/profile",

        method: "POST",

        data: formData,

        processData: false,

        contentType: false,

		success: function (profiles) {

		    console.log(
		        "Profiling results:",
		        profiles
		    );

		    displayProfileResults(
		        profiles
		    );

		    saveSourceProfile(
		        file,
		        sheetName
		    );
		},

        error: function (xhr) {

            console.error(xhr);

            showUploadMessage(
                "Failed to profile Excel file.",
                "error"
            );
        }

    });
}

function saveSourceProfile(
    file,
    sheetName
) {

    const formData =
        new FormData();

    formData.append(
        "file",
        file
    );

    formData.append(
        "sheet",
        sheetName
    );

    showUploadMessage(
        "Saving source profile...",
        "info"
    );

    $.ajax({

        url: "/api/excel/profile/save",

        method: "POST",

        data: formData,

        processData: false,

        contentType: false,

        success: function (sourceProfile) {

            console.log(
                "Saved SourceProfile:",
                sourceProfile
            );

            if (!sourceProfile || !sourceProfile.id) {

                showUploadMessage(
                    "Profile was saved, but no Source Profile ID was returned.",
                    "error"
                );

                return;
            }

            $("#sourceProfileId")
                .val(sourceProfile.id);

            showUploadMessage(
                "Excel file profiled and saved successfully. Source Profile ID: "
                    + sourceProfile.id,
                "success"
            );
        },

        error: function (xhr) {

            console.error(xhr);

            showUploadMessage(
                "Profiling succeeded, but saving the source profile failed.",
                "error"
            );
        }

    });
}

function displayProfileResults(columns) {

    const tbody =
        $("#profileTable tbody");

    tbody.empty();


    if (!columns || columns.length === 0) {

        tbody.append(
            $("<tr>").append(
                $("<td>")
                    .attr("colspan", "3")
                    .text(
                        "No profiling results returned."
                    )
            )
        );

        return;
    }


    columns.forEach(function (column) {

        const row =
            $("<tr>");


        row.append(
            $("<td>").text(
                column.columnName || ""
            )
        );


        row.append(
            $("<td>").text(
                column.dataType || ""
            )
        );


        let samples =
            column.sampleValues || "";


        if (Array.isArray(samples)) {

            samples =
                samples.join(", ");

        }


        row.append(
            $("<td>").text(samples)
        );


        tbody.append(row);
    });
}

function showUploadMessage(
    message,
    type
) {

    $("#uploadMessage")
        .removeClass(
            "success error info"
        )
        .addClass(type)
        .text(message);
}