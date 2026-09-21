$(document).ready(function () {

    $("#loadTargetFields").click(function () {

        const templateVersionId =
            $("#designerTemplateVersionId").val();

        if (!templateVersionId) {

            showDesignerMessage(
                "Please enter a Template Version ID.",
                "error"
            );

            return;
        }

        loadTargetFields(templateVersionId);
    });


    // Automatically load version 1 for now
    loadTargetFields(
        $("#designerTemplateVersionId").val()
    );
	
	$("#addTargetField").click(function () {

	    editingTargetFieldId = null;

	    clearTargetFieldForm();

	    $("#targetFieldForm h3")
	        .text("Add Target Field");

	    $("#saveTargetField")
	        .text("Save Field");

	    $("#targetFieldForm").show();
	});

	$("#cancelTargetField").click(function () {

	    editingTargetFieldId = null;

	    $("#targetFieldForm").hide();

	    clearTargetFieldForm();

	    $("#targetFieldForm h3")
	        .text("Add Target Field");

	    $("#saveTargetField")
	        .text("Save Field");
	});
	
	$("#saveTargetField").click(function () {

	    const templateVersionId =
	        $("#designerTemplateVersionId").val();

	    const name =
	        $("#fieldName").val().trim();

	    const key =
	        $("#fieldKey").val().trim();


			if (!name || !key) {
			    showDesignerMessage(
			        "Name and key are required.",
			        "error"
			    );
			    return;
			}

	    const request = {

	        templateVersionId:
	            parseInt(templateVersionId),

	        name: name,

	        key: key,

	        dataType:
	            $("#fieldDataType").val(),

	        mandatory:
	            $("#fieldMandatory").is(":checked"),

	        defaultValue:
	            $("#fieldDefaultValue").val().trim() || null,

	        description:
	            $("#fieldDescription").val().trim() || null
	    };

		const isEditing =
		    editingTargetFieldId !== null;

		const url =
		    isEditing
		        ? "/api/target-fields/" + editingTargetFieldId
		        : "/api/target-fields";

		const method =
		    isEditing
		        ? "PUT"
		        : "POST";
		
		$.ajax({

		    url: url,

		    method: method,

		    contentType: "application/json",

		    data: JSON.stringify(request),

		    success: function () {

		        const message =
		            isEditing
		                ? "Target field updated successfully."
		                : "Target field created successfully.";

		        editingTargetFieldId = null;

		        $("#targetFieldForm").hide();

		        clearTargetFieldForm();

		        loadTargetFields(
		            templateVersionId
		        );

		        showDesignerMessage(
		            message,
		            "success"
		        );
		    },

		    error: function (xhr) {

		        console.error(xhr);

		        showDesignerMessage(
		            isEditing
		                ? "Could not update target field."
		                : "Could not create target field.",
		            "error"
		        );
		    }
		});
	});
});

let editingTargetFieldId = null;

function loadTargetFields(templateVersionId) {

    $.ajax({

        url: "/api/target-fields",

        method: "GET",

        data: {
            templateVersionId:
                templateVersionId
        },

        success: function (fields) {

            displayTargetFields(fields);

            showDesignerMessage(
                "Target fields loaded successfully.",
                "success"
            );
        },

        error: function (xhr) {

            console.error(xhr);

            $("#targetFieldsTable tbody")
                .empty();

            showDesignerMessage(
                "Could not load target fields.",
                "error"
            );
        }
    });
}


function displayTargetFields(fields) {

    const tbody =
        $("#targetFieldsTable tbody");

    tbody.empty();

    if (!fields || fields.length === 0) {

        tbody.append(
            $("<tr>").append(
                $("<td>")
                    .attr("colspan", "8")
                    .text(
                        "No target fields found."
                    )
            )
        );

        return;
    }

    fields.forEach(function (field, index) {

        const row =
            $("<tr>");

        row.append(
            $("<td>").text(
                field.sequence
            )
        );

        row.append(
            $("<td>").text(
                field.name
            )
        );

        row.append(
            $("<td>").text(
                field.key
            )
        );

        row.append(
            $("<td>").text(
                field.dataType
            )
        );

        row.append(
            $("<td>").text(
                field.mandatory
                    ? "Yes"
                    : "No"
            )
        );

        row.append(
            $("<td>").text(
                field.defaultValue || ""
            )
        );

        row.append(
            $("<td>").text(
                field.description || ""
            )
        );
		
		const moveUpButton =
		    $("<button>")
		        .addClass("move-field-button")
		        .text("↑")
		        .attr("title", "Move Up")
		        .prop("disabled", index === 0)
		        .click(function () {
		            moveTargetField(field.id, "move-up");
		        });

		const moveDownButton =
		    $("<button>")
		        .addClass("move-field-button")
		        .text("↓")
		        .attr("title", "Move Down")
		        .prop("disabled", index === fields.length - 1)
		        .click(function () {
		            moveTargetField(field.id, "move-down");
		        });

		const editButton =
		    $("<button>")
		        .addClass("edit-field-button")
		        .text("Edit")
		        .click(function () {
		            editTargetField(field);
		        });

		const deleteButton =
		    $("<button>")
		        .addClass("delete-field-button")
		        .text("Delete")
		        .click(function () {
		            deleteTargetField(field);
		        });

		const actionCell =
				    $("<td>")
				        .append(moveUpButton)
				        .append(moveDownButton)
				        .append(editButton)
				        .append(deleteButton);

				row.append(actionCell);

        tbody.append(row);
    });
}

function moveTargetField(fieldId, direction) {

    $.ajax({

        url:
            "/api/target-fields/" +
            fieldId +
            "/" +
            direction,

        method: "PUT",

        success: function () {

            const templateVersionId =
                $("#designerTemplateVersionId").val();

            loadTargetFields(
                templateVersionId
            );

            showDesignerMessage(
                "Target field order updated.",
                "success"
            );
        },

        error: function (xhr) {

            console.error(xhr);

            showDesignerMessage(
                "Could not update target field order.",
                "error"
            );
        }
    });
}

function editTargetField(field) {

    editingTargetFieldId = field.id;

    $("#fieldName")
        .val(field.name);

    $("#fieldKey")
        .val(field.key);

    $("#fieldDataType")
        .val(field.dataType);


    $("#fieldMandatory")
        .prop(
            "checked",
            field.mandatory
        );

    $("#fieldDefaultValue")
        .val(
            field.defaultValue || ""
        );

    $("#fieldDescription")
        .val(
            field.description || ""
        );

    $("#targetFieldForm h3")
        .text("Edit Target Field");

    $("#saveTargetField")
        .text("Update Field");

    $("#targetFieldForm").show();
}

function deleteTargetField(field) {

    const confirmed =
        confirm(
            'Are you sure you want to delete "' +
            field.name +
            '"?'
        );

    if (!confirmed) {
        return;
    }

    $.ajax({

        url:
            "/api/target-fields/" +
            field.id,

        method: "DELETE",

        success: function () {

            const templateVersionId =
                $("#designerTemplateVersionId").val();

            loadTargetFields(
                templateVersionId
            );

            showDesignerMessage(
                "Target field deleted successfully.",
                "success"
            );
        },

        error: function (xhr) {

            console.error(xhr);

            showDesignerMessage(
                "Target field could not be deleted because it may already be in use.",
                "error"
            );
        }
    });
}

function showDesignerMessage(
    message,
    type
) {

    const container =
        $("#designerMessage");

    container
        .removeClass(
            "designer-success designer-error"
        )
        .text(message);

    if (type === "success") {
        container.addClass(
            "designer-success"
        );
    }

    if (type === "error") {
        container.addClass(
            "designer-error"
        );
    }
}

function clearTargetFieldForm() {

    $("#fieldName").val("");
    $("#fieldKey").val("");
    $("#fieldDataType").val("STRING");
    $("#fieldSequence").val("");
    $("#fieldMandatory").prop("checked", false);
    $("#fieldDefaultValue").val("");
    $("#fieldDescription").val("");
}