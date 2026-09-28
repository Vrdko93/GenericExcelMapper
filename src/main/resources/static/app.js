$(document).ready(function () {

	const templateVersionId =
	    $("#templateVersionId").val();

	if (templateVersionId) {
	    loadMappingProfiles(
	        templateVersionId
	    );
	}
	
	$("#templateVersionId").change(function () {

	    const templateVersionId =
	        $(this).val();

	    loadMappingProfiles(
	        templateVersionId
	    );
	});
	
	$("#uploadExcel").click(function () {

	    uploadExcel();

	});
	
	$("#loadSuggestions").click(function () {

	    const sourceProfileId =
	        $("#sourceProfileId").val();

	    const templateVersionId =
	        $("#templateVersionId").val();

	    const mappingProfileId =
	        $("#mappingProfileId").val();

	    if (!sourceProfileId || !templateVersionId) {

	        showMessage(
	            "Please enter Source Profile ID and Template Version ID.",
	            "error"
	        );

	        return;
	    }

	    $.ajax({

	        url: "/api/mappings/review",

	        method: "GET",

	        data: {
	            sourceProfileId: sourceProfileId
	        },

	        success: function (mappings) {

	            if (mappings.length > 0) {

	                // Existing accepted mappings
	                displayMappingReview(mappings);

	                $("#suggestionsTable tbody").empty();

	                showMessage(
	                    "Existing mappings loaded.",
	                    "success"
	                );

	                return;
	            }

	            // No accepted mappings yet.
	            // Generate suggestions normally.
	            loadSuggestions(
	                sourceProfileId,
	                templateVersionId,
	                mappingProfileId
	            );
	        },

	        error: function (xhr) {

	            console.error(xhr);

	            showMessage(
	                "Could not check existing mappings.",
	                "error"
	            );
	        }
	    });
	});

	$("#previewMappedData").click(function () {

	    const file =
	        $("#excelFile")[0].files[0];

	    const sourceProfileId =
	        $("#sourceProfileId").val();

	    if (!file) {
	        showMessage(
	            "Please upload an Excel file first.",
	            "error"
	        );
	        return;
	    }

	    if (!sourceProfileId) {
	        showMessage(
	            "Source profile is missing.",
	            "error"
	        );
	        return;
	    }

	    const formData = new FormData();

	    formData.append("file", file);
	    formData.append("sheet", sheetName);
	    formData.append(
	        "sourceProfileId",
	        sourceProfileId
	    );

	    $.ajax({

	        url: "/api/mappings/preview",

	        method: "POST",

	        data: formData,

	        processData: false,

	        contentType: false,

	        success: function (rows) {

	            displayMappedPreview(rows);
	        },

	        error: function (xhr) {

	            console.error(xhr);

	            showMessage(
	                "Could not generate preview.",
	                "error"
	            );
	        }
	    });
	});
	
	$("#validateMappedData").click(function () {

	    const file =
	        $("#excelFile")[0].files[0];

	    const sourceProfileId =
	        $("#sourceProfileId").val();

	    const templateVersionId =
	        $("#templateVersionId").val();

	    if (!file || !sourceProfileId) {
	        showMessage(
	            "Please upload and map an Excel file first.",
	            "error"
	        );
	        return;
	    }

	    const formData = new FormData();

	    formData.append("file", file);
	    formData.append("sheet", sheetName);
	    formData.append(
	        "sourceProfileId",
	        sourceProfileId
	    );
	    formData.append(
	        "templateVersionId",
	        templateVersionId
	    );

	    $.ajax({
	        url: "/api/mappings/validate",
	        method: "POST",
	        data: formData,
	        processData: false,
	        contentType: false,

	        success: function (errors) {
	            displayValidationResults(errors);
	        },

	        error: function (xhr) {
	            console.error(xhr);

	            showMessage(
	                "Could not validate mapped data.",
	                "error"
	            );
	        }
	    });
	});
	
	$("#exportExcel").click(function () {
		
		$("#exportMessage")
		    .removeClass(
		        "export-error export-success"
		    )
		    .text("");

	    const file =
	        $("#excelFile")[0].files[0];

	    const sourceProfileId =
	        $("#sourceProfileId").val();

	    const templateVersionId =
	        $("#templateVersionId").val();

	    if (!file || !sourceProfileId) {

	        showMessage(
	            "Please upload and map an Excel file first.",
	            "error"
	        );

	        return;
	    }

	    const formData = new FormData();

	    formData.append("file", file);
	    formData.append("sheet", sheetName);
	    formData.append(
	        "sourceProfileId",
	        sourceProfileId
	    );
	    formData.append(
	        "templateVersionId",
	        templateVersionId
	    );

	    // Validate BEFORE attempting export
	    $.ajax({

	        url: "/api/mappings/validate",
	        method: "POST",
	        data: formData,
	        processData: false,
	        contentType: false,

	        success: function (errors) {

	            if (errors && errors.length > 0) {

	                displayValidationResults(errors);

					$("#exportMessage")
					    .removeClass("export-success")
					    .addClass("export-error")
					    .text(
					        "Export blocked. Please fix validation errors first."
					    );

	                return;
	            }

	            // Validation passed
	            exportMappedExcel(
	                file,
	                sourceProfileId,
	                templateVersionId
	            );
	        },

	        error: function (xhr) {

	            console.error(xhr);

	            showMessage(
	                "Could not validate data before export.",
	                "error"
	            );
	        }
	    });
	});
});

let availableTargetFields = [];
let sheetName = null;

function displayValidationResults(errors) {

    const container =
        $("#validationResults");

    container.empty();

    if (!errors || errors.length === 0) {

        container.append(
            $("<div>")
                .addClass("validation-success")
                .text(
                    "Validation passed. No errors found."
                )
        );

        return;
    }

    container.append(
        $("<h3>").text(
            "Validation Errors (" +
            errors.length +
            ")"
        )
    );

    const table =
        $("<table>")
            .addClass("validation-table");

    const header =
        $("<tr>")
            .append($("<th>").text("Excel Row"))
            .append($("<th>").text("Target Field"))
            .append($("<th>").text("Error"));

    table.append(
        $("<thead>").append(header)
    );

    const tbody = $("<tbody>");

    errors.forEach(function (error) {

        const row =
            $("<tr>")
                .append(
                    $("<td>").text(
                        error.rowNumber
                    )
                )
                .append(
                    $("<td>").text(
                        error.targetField
                    )
                )
                .append(
                    $("<td>").text(
                        error.message
                    )
                );

        tbody.append(row);
    });

    table.append(tbody);

    container.append(table);
}

function loadMappingProfiles(
    templateVersionId
) {

    $.ajax({

        url: "/api/mapping-profiles",

        method: "GET",

        data: {
            templateVersionId: templateVersionId
        },

        success: function (profiles) {

            const select =
                $("#mappingProfileId");

            select.empty();

            select.append(
                $("<option>")
                    .val("")
                    .text("No Profile")
            );

            profiles.forEach(function (profile) {

                select.append(
                    $("<option>")
                        .val(profile.id)
                        .text(profile.name)
                );
            });
        },

        error: function (xhr) {

            console.error(xhr);

            showMessage(
                "Failed to load mapping profiles.",
                "error"
            );
        }
    });
}



function loadTargetFields(templateVersionId, callback) {

    $.ajax({

        url: "/api/target-fields",

        method: "GET",

        data: {
            templateVersionId: templateVersionId
        },

        success: function (targetFields) {

            availableTargetFields =
                targetFields;

            if (callback) {
                callback();
            }
        },

        error: function (xhr) {

            console.error(xhr);

            showMessage(
                "Failed to load target fields.",
                "error"
            );
        }
    });
}

function loadSuggestions(
    sourceProfileId,
    templateVersionId
) {

	const mappingProfileId =
	    $("#mappingProfileId").val();
	
    showMessage(
        "Loading mapping suggestions...",
        "info"
    );

    loadTargetFields(
        templateVersionId,
        function () {
            fetchMappingSuggestions(
                sourceProfileId,
                templateVersionId,
				mappingProfileId
            );
        }
    );
}

function fetchMappingSuggestions(
    sourceProfileId,
    templateVersionId,
	mappingProfileId
) {

    $("#mappingTable tbody").empty();
	
	const requestData = {
	    sourceProfileId: sourceProfileId,
	    templateVersionId: templateVersionId
	};

	if (mappingProfileId) {
	    requestData.mappingProfileId =
	        mappingProfileId;
	}

    $.ajax({

        url: "/api/mapping-suggestions",

        method: "GET",

        data: {
            sourceProfileId: sourceProfileId,
            templateVersionId: templateVersionId,
			mappingProfileId: mappingProfileId
        },

        success: function (suggestions) {

            $("#mappingTable tbody").empty();

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

	const targetSelect =
	    $("<select>")
	        .addClass("target-select");

	availableTargetFields.forEach(function (field) {

	    const option =
	        $("<option>")
	            .val(field.id)
	            .text(field.name);

	    if (field.id === suggestion.targetFieldId) {
	        option.prop("selected", true);
	    }

	    targetSelect.append(option);
	});

	row.append(
	    $("<td>").append(targetSelect)
	);

    row.append(
        $("<td>").text(
            suggestion.targetDataType
        )
    );

	const methodCell =
	    $("<td>");

	const methodBadge =
	    $("<span>")
	        .addClass("method-badge")
	        .text(suggestion.mappingMethod);

	if (suggestion.mappingMethod === "AI") {
	    methodBadge.addClass("method-ai");
	}
	else if (suggestion.mappingMethod === "DETERMINISTIC") {
	    methodBadge.addClass("method-deterministic");
	}
	else if (suggestion.mappingMethod === "MANUAL") {
	    methodBadge.addClass("method-manual");
	}

	methodCell.append(methodBadge);

	row.append(methodCell);
	
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

	const transformationSelect =
	    $("<select>")
	        .addClass("transformation-select");

	const transformations = [
	    "DIRECT",
	    "DEFAULT",
	    "CONSTANT",
	    "TRIM",
	    "UPPERCASE",
	    "LOWERCASE",
	    "DATE_FORMAT",
		"CURRENCY_TO_NUMBER"
	];

	transformations.forEach(function (transformation) {

	    transformationSelect.append(
	        $("<option>")
	            .val(transformation)
	            .text(transformation)
	    );
	});
	
	const transformationConfig =
	    $("<input>")
	        .attr("type", "text")
	        .addClass("transformation-config")
	        .hide();


	
	transformationSelect.change(function () {

	    const transformation =
	        $(this).val();

	    transformationConfig
	        .val("")
	        .hide();

	    if (transformation === "DEFAULT") {

	        transformationConfig
	            .attr("placeholder", "Default value")
	            .show();
	    }

	    else if (transformation === "CONSTANT") {

	        transformationConfig
	            .attr("placeholder", "Constant value")
	            .show();
	    }

	    else if (transformation === "DATE_FORMAT") {

	        transformationConfig
	            .attr(
	                "placeholder",
	                "e.g. M/d/yy -> yyyy-MM-dd"
	            )
	            .show();
	    }
	});
	
	const transformationCell =
	    $("<td>")
	        .append(transformationSelect)
	        .append(transformationConfig);

	row.append(transformationCell);

    const actionCell = $("<td>");

    const acceptButton =
        $("<button>")
            .addClass("accept-button")
            .text("Accept");

			acceptButton.click(function () {

			    const selectedTargetFieldId =
			        parseInt(
			            targetSelect.val()
			        );

				const selectedTransformation =
					   transformationSelect.val();
					   
				const selectedTransformationConfig =
					   transformationConfig.val();
					
			    acceptMapping(
			        suggestion,
			        selectedTargetFieldId,
					selectedTransformation,
					selectedTransformationConfig,
			        row
			    );
			});

    actionCell.append(acceptButton);

    row.append(actionCell);

    $("#mappingTable tbody").append(row);
}


function acceptMapping(
    suggestion,
    selectedTargetFieldId,
	selectedTransformation,
	selectedTransformationConfig,
    row
) {

	const wasOverridden =
	    selectedTargetFieldId !== suggestion.targetFieldId;

	const originallyAi =
	    suggestion.mappingMethod === "AI";

	const request = {
	    sourceColumnId: suggestion.sourceColumnId,
	    targetFieldId: selectedTargetFieldId,
	    transformation: selectedTransformation,
	    transformationConfig:
	        selectedTransformationConfig || null,

	    mappingMethod:
	        wasOverridden
	            ? "MANUAL"
	            : suggestion.mappingMethod,

	    aiConfidence:
	        originallyAi
	            ? suggestion.confidence
	            : null,

	    aiRecommendedTargetFieldId:
	        originallyAi
	            ? suggestion.targetFieldId
	            : null
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
                .text(
                    wasOverridden
                        ? "Overridden"
                        : "Accepted"
                );

            targetSelectDisabled(row);

			row.find(".transformation-select")
			    .prop("disabled", true);
				
			row.find(".transformation-config")
				.prop("disabled", true);
			
            showMessage(
                wasOverridden
                    ? "Manual override saved."
                    : "Mapping accepted.",
                "success"
            );
			
			loadMappingReview();
        },

        error: function (xhr) {

            console.error(xhr);

            showMessage(
                "Could not save mapping.",
                "error"
            );
        }
    });
}

function targetSelectDisabled(row) {

    row.find(".target-select")
        .prop("disabled", true);
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


            sheetName =
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

function displayMappedPreview(rows) {

    const table =
        $("#previewTable");

    const thead =
        table.find("thead");

    const tbody =
        table.find("tbody");

    thead.empty();
    tbody.empty();

    if (!rows || rows.length === 0) {

        showMessage(
            "No preview data available.",
            "error"
        );

        return;
    }

    const headers =
        Object.keys(rows[0]);

    const headerRow =
        $("<tr>");

    headers.forEach(function (header) {

        headerRow.append(
            $("<th>").text(header)
        );
    });

    thead.append(headerRow);

    rows.forEach(function (row) {

        const tableRow =
            $("<tr>");

        headers.forEach(function (header) {

            tableRow.append(
                $("<td>").text(
                    row[header] ?? ""
                )
            );
        });

        tbody.append(tableRow);
    });

    $("#previewSection").show();
}

function saveSourceProfile(file, sheetName) {

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

            $("#sourceProfileId").val(sourceProfile.id);
			
			showUploadMessage(
			    "Excel file profiled and saved successfully. Source Profile ID: "
			        + sourceProfile.id,
			    "success"
			);
			
			const templateVersionId =
			    $("#templateVersionId").val();

			if (templateVersionId) {

			    loadSuggestions(
			        sourceProfile.id,
			        templateVersionId
			    );
			}

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

function exportMappedExcel(
    file,
    sourceProfileId,
    templateVersionId
) {

    const formData =
        new FormData();

    formData.append("file", file);
    formData.append(
        "sheet",
        sheetName
    );
    formData.append(
        "sourceProfileId",
        sourceProfileId
    );
    formData.append(
        "templateVersionId",
        templateVersionId
    );

    $.ajax({

        url: "/api/mappings/export",
        method: "POST",

        data: formData,

        processData: false,
        contentType: false,

        xhrFields: {
            responseType: "blob"
        },

        success: function (data) {

            const blob =
                new Blob(
                    [data],
                    {
                        type:
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    }
                );

            const url =
                window.URL.createObjectURL(
                    blob
                );

            const link =
                document.createElement("a");

            link.href = url;

            link.download =
                "mapped-output.xlsx";

            document.body.appendChild(
                link
            );

            link.click();

            link.remove();

            window.URL.revokeObjectURL(
                url
            );

			$("#exportMessage")
			    .removeClass("export-error")
			    .addClass("export-success")
			    .text(
			        "Excel file exported successfully."
			    );
        },

        error: function (xhr) {

            console.error(xhr);

			$("#exportMessage")
			    .removeClass("export-success")
			    .addClass("export-error")
			    .text(
			        "Excel export failed."
			    );
        }
    });
}

function loadMappingReview() {

    const profileId =
        $("#sourceProfileId").val();

    if (!profileId) {
        return;
    }

    $.ajax({

        url: "/api/mappings/review",

        method: "GET",

        data: {
            sourceProfileId: profileId
        },

        success: function (mappings) {

            displayMappingReview(mappings);
        },

        error: function (xhr) {

            console.error(
                "Mapping review failed:",
                xhr.status,
                xhr.responseText
            );

            showMessage(
                "Unable to load mapping review.",
                "error"
            );
        }
    });
}

function displayMappingReview(mappings) {

    const tbody =
        $("#mappingReviewTable tbody");

    tbody.empty();
	
	if (mappings.length === 0) {

	    $("#mappingReviewSection").hide();

	    return;
	}

    mappings.forEach(function (mapping) {

        const row = $("<tr>");

        row.append(
            $("<td>").text(
                mapping.sourceColumnName
            )
        );

        row.append(
            $("<td>").text(
                mapping.targetFieldName
            )
        );

        row.append(
            $("<td>").text(
                mapping.mappingMethod
            )
        );

        row.append(
            $("<td>").text(
                mapping.aiRecommendedTargetFieldName || "—"
            )
        );

        row.append(
            $("<td>").text(
                mapping.aiConfidence != null
                    ? mapping.aiConfidence + "%"
                    : "—"
            )
        );

        let transformation =
            mapping.transformation || "DIRECT";

        if (mapping.transformationConfig) {
            transformation +=
                " (" +
                mapping.transformationConfig +
                ")";
        }

        row.append(
            $("<td>").text(transformation)
        );

        tbody.append(row);
    });

    $("#mappingReviewSection").show();
}

function clearMappingReview() {

    $("#mappingReviewTable tbody").empty();

    $("#mappingReviewSection").hide();
}