(function() {

	var A = AUI();

	var baseJscPluginPath = themeDisplay.getPathJavaScript() +
		'/editor/ckeditor/plugins/liferayspellchecker';

	var jscCssPath = baseJscPluginPath + '/css/liferay.spellchecker.css';

	CKEDITOR.document.appendStyleSheet(CKEDITOR.getUrl(jscCssPath));

	CKEDITOR.config.contentsCss = [CKEDITOR.config.contentsCss, jscCssPath];

	CKEDITOR.plugins.add(
		'liferayspellchecker',
		{
			config: {
				lang: 'en',
				parser: 'html',
				suggestBox: {
					appendTo: 'body',
					position: 'below'
				},
				webservice: {
					driver: 'liferay'
				}
			},

			create: function() {
				var instance = this;

				instance.editor.setReadOnly(true);
				instance.editor.commands.liferayspellchecker.toggleState();
				instance.editorWindow = this.editor.document.getWindow().$;

				instance.createSpellchecker();
				// instance.spellchecker.check();
				instance.spellchecker.checkSpelling();
			},

			createSpellchecker: function() {
				var instance = this;

				instance.config.getText = function() {
					var node = A.Node.create('<div />');
					var text = instance.editor.getData();

					return node.append(text).attr('textContent');
				};

				instance.spellchecker = new A.SpellChecker(
					instance.editor.document.$.body,
					instance.config
				);

				// TODO: add alert for no incorrectly spelled words

				/*instance.spellchecker.on(
					'check.success',
					function() {
						alert(Liferay.Language.get('there-are-no-incorrectly-spelled-words'));

						instance.destroy();
					}
				);*/
			},

			destroy: function() {
				var instance = this;

				if (!this.spellchecker) {
					return;
				}

				instance.config.getText = null;

				instance.spellchecker.destroy();
				instance.spellchecker = null;

				instance.editor.setReadOnly(false);
				instance.editor.commands.liferayspellchecker.toggleState();
			},

			init: function(editor) {
				var instance = this;

				var dependency = CKEDITOR.getUrl(instance.path + 'js/new.liferay.spellchecker.js');

				CKEDITOR.scriptLoader.load(dependency);

				editor.addCommand(
					'liferayspellchecker',
					{
						canUndo: false,
						exec: function() {
							instance.toggle(editor);
						},
						readOnly: 1
					}
				);

				editor.ui.addButton(
					'LiferaySpellChecker',
					{
						command: 'liferayspellchecker',
						icon: baseJscPluginPath + '/assets/spellchecker.png',
						label: Liferay.Language.get('spell-check'),
						toolbar: 'spellchecker,10'
					}
				);

				editor.on(
					'saveSnapshot',
					function() {
						instance.destroy();
					}
				);
			},

			toggle: function(editor) {
				var instance = this;

				instance.editor = editor;

				if (!instance.spellchecker) {
					instance.create();
				}
				else {
					instance.destroy();
				}
			}
		}
	);

})();