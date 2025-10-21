(global["webpackJsonp"] = global["webpackJsonp"] || []).push([["pages/service/hospital-detail"],{

/***/ 314:
/*!*********************************************************************************************************!*\
  !*** C:/Users/Yu/Desktop/pet-home/pet-home-uniapp/main.js?{"page":"pages%2Fservice%2Fhospital-detail"} ***!
  \*********************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function(wx, createPage) {

var _interopRequireDefault = __webpack_require__(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
__webpack_require__(/*! uni-pages */ 26);
var _vue = _interopRequireDefault(__webpack_require__(/*! vue */ 25));
var _hospitalDetail = _interopRequireDefault(__webpack_require__(/*! ./pages/service/hospital-detail.vue */ 315));
// @ts-ignore
wx.__webpack_require_UNI_MP_PLUGIN__ = __webpack_require__;
createPage(_hospitalDetail.default);
/* WEBPACK VAR INJECTION */}.call(this, __webpack_require__(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1)["default"], __webpack_require__(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)["createPage"]))

/***/ }),

/***/ 315:
/*!**************************************************************************************!*\
  !*** C:/Users/Yu/Desktop/pet-home/pet-home-uniapp/pages/service/hospital-detail.vue ***!
  \**************************************************************************************/
/*! no static exports found */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _hospital_detail_vue_vue_type_template_id_5d3fc6fe_scoped_true___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./hospital-detail.vue?vue&type=template&id=5d3fc6fe&scoped=true& */ 316);
/* harmony import */ var _hospital_detail_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./hospital-detail.vue?vue&type=script&lang=js& */ 318);
/* harmony reexport (unknown) */ for(var __WEBPACK_IMPORT_KEY__ in _hospital_detail_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_1__) if(["default"].indexOf(__WEBPACK_IMPORT_KEY__) < 0) (function(key) { __webpack_require__.d(__webpack_exports__, key, function() { return _hospital_detail_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_1__[key]; }) }(__WEBPACK_IMPORT_KEY__));
/* harmony import */ var _hospital_detail_vue_vue_type_style_index_0_id_5d3fc6fe_lang_scss_scoped_true___WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./hospital-detail.vue?vue&type=style&index=0&id=5d3fc6fe&lang=scss&scoped=true& */ 320);
/* harmony import */ var _D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_runtime_componentNormalizer_js__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 34);

var renderjs





/* normalize component */

var component = Object(_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_runtime_componentNormalizer_js__WEBPACK_IMPORTED_MODULE_3__["default"])(
  _hospital_detail_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_1__["default"],
  _hospital_detail_vue_vue_type_template_id_5d3fc6fe_scoped_true___WEBPACK_IMPORTED_MODULE_0__["render"],
  _hospital_detail_vue_vue_type_template_id_5d3fc6fe_scoped_true___WEBPACK_IMPORTED_MODULE_0__["staticRenderFns"],
  false,
  null,
  "5d3fc6fe",
  null,
  false,
  _hospital_detail_vue_vue_type_template_id_5d3fc6fe_scoped_true___WEBPACK_IMPORTED_MODULE_0__["components"],
  renderjs
)

component.options.__file = "pages/service/hospital-detail.vue"
/* harmony default export */ __webpack_exports__["default"] = (component.exports);

/***/ }),

/***/ 316:
/*!*********************************************************************************************************************************!*\
  !*** C:/Users/Yu/Desktop/pet-home/pet-home-uniapp/pages/service/hospital-detail.vue?vue&type=template&id=5d3fc6fe&scoped=true& ***!
  \*********************************************************************************************************************************/
/*! exports provided: render, staticRenderFns, recyclableRender, components */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_17_0_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_template_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_uni_app_loader_page_meta_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_hospital_detail_vue_vue_type_template_id_5d3fc6fe_scoped_true___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./hospital-detail.vue?vue&type=template&id=5d3fc6fe&scoped=true& */ 317);
/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "render", function() { return _D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_17_0_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_template_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_uni_app_loader_page_meta_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_hospital_detail_vue_vue_type_template_id_5d3fc6fe_scoped_true___WEBPACK_IMPORTED_MODULE_0__["render"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "staticRenderFns", function() { return _D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_17_0_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_template_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_uni_app_loader_page_meta_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_hospital_detail_vue_vue_type_template_id_5d3fc6fe_scoped_true___WEBPACK_IMPORTED_MODULE_0__["staticRenderFns"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "recyclableRender", function() { return _D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_17_0_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_template_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_uni_app_loader_page_meta_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_hospital_detail_vue_vue_type_template_id_5d3fc6fe_scoped_true___WEBPACK_IMPORTED_MODULE_0__["recyclableRender"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "components", function() { return _D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_17_0_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_template_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_uni_app_loader_page_meta_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_hospital_detail_vue_vue_type_template_id_5d3fc6fe_scoped_true___WEBPACK_IMPORTED_MODULE_0__["components"]; });



/***/ }),

/***/ 317:
/*!*********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
  !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!C:/Users/Yu/Desktop/pet-home/pet-home-uniapp/pages/service/hospital-detail.vue?vue&type=template&id=5d3fc6fe&scoped=true& ***!
  \*********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
/*! exports provided: render, staticRenderFns, recyclableRender, components */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "render", function() { return render; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "staticRenderFns", function() { return staticRenderFns; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "recyclableRender", function() { return recyclableRender; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "components", function() { return components; });
var components
var render = function () {
  var _vm = this
  var _h = _vm.$createElement
  var _c = _vm._self._c || _h
  var m0 = _vm.getImageUrl(_vm.serviceData.bannerImage)
  _vm.$mp.data = Object.assign(
    {},
    {
      $root: {
        m0: m0,
      },
    }
  )
}
var recyclableRender = false
var staticRenderFns = []
render._withStripped = true



/***/ }),

/***/ 318:
/*!***************************************************************************************************************!*\
  !*** C:/Users/Yu/Desktop/pet-home/pet-home-uniapp/pages/service/hospital-detail.vue?vue&type=script&lang=js& ***!
  \***************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _D_HBuilderX_plugins_uniapp_cli_node_modules_babel_loader_lib_index_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_13_1_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_script_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_hospital_detail_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./hospital-detail.vue?vue&type=script&lang=js& */ 319);
/* harmony import */ var _D_HBuilderX_plugins_uniapp_cli_node_modules_babel_loader_lib_index_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_13_1_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_script_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_hospital_detail_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_D_HBuilderX_plugins_uniapp_cli_node_modules_babel_loader_lib_index_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_13_1_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_script_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_hospital_detail_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__);
/* harmony reexport (unknown) */ for(var __WEBPACK_IMPORT_KEY__ in _D_HBuilderX_plugins_uniapp_cli_node_modules_babel_loader_lib_index_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_13_1_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_script_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_hospital_detail_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__) if(["default"].indexOf(__WEBPACK_IMPORT_KEY__) < 0) (function(key) { __webpack_require__.d(__webpack_exports__, key, function() { return _D_HBuilderX_plugins_uniapp_cli_node_modules_babel_loader_lib_index_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_13_1_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_script_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_hospital_detail_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__[key]; }) }(__WEBPACK_IMPORT_KEY__));
 /* harmony default export */ __webpack_exports__["default"] = (_D_HBuilderX_plugins_uniapp_cli_node_modules_babel_loader_lib_index_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_13_1_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_script_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_hospital_detail_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0___default.a); 

/***/ }),

/***/ 319:
/*!**********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
  !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!C:/Users/Yu/Desktop/pet-home/pet-home-uniapp/pages/service/hospital-detail.vue?vue&type=script&lang=js& ***!
  \**********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function(uni) {

var _interopRequireDefault = __webpack_require__(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var _regenerator = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/regenerator */ 67));
var _asyncToGenerator2 = _interopRequireDefault(__webpack_require__(/*! @babel/runtime/helpers/asyncToGenerator */ 69));
var _util = __webpack_require__(/*! @/common/js/util.js */ 45);
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
var _default = {
  name: 'HospitalDetail',
  data: function data() {
    return {
      serviceData: {
        id: null,
        name: '',
        price: 0,
        bannerImage: '',
        introduction: [],
        instructions: []
      },
      serviceType: ''
    };
  },
  onLoad: function onLoad(options) {
    if (options.serviceType) {
      this.serviceType = options.serviceType;
      this.loadServiceData();
    }
  },
  methods: {
    loadServiceData: function loadServiceData() {
      var _this = this;
      return (0, _asyncToGenerator2.default)( /*#__PURE__*/_regenerator.default.mark(function _callee() {
        return _regenerator.default.wrap(function _callee$(_context) {
          while (1) {
            switch (_context.prev = _context.next) {
              case 0:
                _context.prev = 0;
                _context.t0 = _this.serviceType;
                _context.next = _context.t0 === 'hospital' ? 4 : _context.t0 === 'litter' ? 7 : _context.t0 === 'boarding' ? 10 : _context.t0 === 'medical' ? 13 : _context.t0 === 'grooming' ? 16 : _context.t0 === 'adoption' ? 19 : 22;
                break;
              case 4:
                _context.next = 6;
                return _this.loadHospitalServiceData();
              case 6:
                return _context.abrupt("break", 23);
              case 7:
                _context.next = 9;
                return _this.loadLitterServiceData();
              case 9:
                return _context.abrupt("break", 23);
              case 10:
                _context.next = 12;
                return _this.loadBoardingServiceData();
              case 12:
                return _context.abrupt("break", 23);
              case 13:
                _context.next = 15;
                return _this.loadMedicalServiceData();
              case 15:
                return _context.abrupt("break", 23);
              case 16:
                _context.next = 18;
                return _this.loadGroomingServiceData();
              case 18:
                return _context.abrupt("break", 23);
              case 19:
                _context.next = 21;
                return _this.loadAdoptionServiceData();
              case 21:
                return _context.abrupt("break", 23);
              case 22:
                uni.showToast({
                  title: '服务类型错误',
                  icon: 'none'
                });
              case 23:
                _context.next = 29;
                break;
              case 25:
                _context.prev = 25;
                _context.t1 = _context["catch"](0);
                console.error('加载服务数据失败:', _context.t1);
                uni.showToast({
                  title: '加载失败',
                  icon: 'none'
                });
              case 29:
              case "end":
                return _context.stop();
            }
          }
        }, _callee, null, [[0, 25]]);
      }))();
    },
    // 加载宠物医院服务数据
    loadHospitalServiceData: function loadHospitalServiceData() {
      var _this2 = this;
      return (0, _asyncToGenerator2.default)( /*#__PURE__*/_regenerator.default.mark(function _callee2() {
        var response, service;
        return _regenerator.default.wrap(function _callee2$(_context2) {
          while (1) {
            switch (_context2.prev = _context2.next) {
              case 0:
                _context2.prev = 0;
                _context2.next = 3;
                return _this2.$api.getHospitalServicePage({
                  current: 1,
                  size: 10,
                  status: 'active'
                });
              case 3:
                response = _context2.sent;
                if (response.code === 0 && response.data && response.data.records && response.data.records.length > 0) {
                  service = response.data.records[0]; // 取第一个服务作为详情
                  _this2.serviceData = {
                    id: service.id,
                    name: service.name,
                    price: service.price,
                    bannerImage: service.imageUrl || '/static/images/hospital-banner.jpg',
                    introduction: _this2.parseDescriptionToList(service.description),
                    instructions: ['购买后凭电子券码预约使用', '需提前一天预约', '请携带宠物健康档案和疫苗接种记录']
                  };
                } else {
                  // 如果API没有数据，使用默认数据
                  _this2.serviceData = {
                    id: 1,
                    name: '宠物医院服务',
                    price: 150,
                    bannerImage: '/static/images/hospital-banner.jpg',
                    introduction: ['专业宠物医疗团队', '先进医疗设备', '24小时急诊服务', '宠物健康体检'],
                    instructions: ['购买后凭电子券码预约使用', '需提前一天预约', '请携带宠物健康档案和疫苗接种记录']
                  };
                }
                _context2.next = 11;
                break;
              case 7:
                _context2.prev = 7;
                _context2.t0 = _context2["catch"](0);
                console.error('加载宠物医院服务数据失败:', _context2.t0);
                // 使用默认数据
                _this2.serviceData = {
                  id: 1,
                  name: '宠物医院服务',
                  price: 150,
                  bannerImage: '/static/images/hospital-banner.jpg',
                  introduction: ['专业宠物医疗团队', '先进医疗设备', '24小时急诊服务', '宠物健康体检'],
                  instructions: ['购买后凭电子券码预约使用', '需提前一天预约', '请携带宠物健康档案和疫苗接种记录']
                };
              case 11:
              case "end":
                return _context2.stop();
            }
          }
        }, _callee2, null, [[0, 7]]);
      }))();
    },
    loadLitterServiceData: function loadLitterServiceData() {
      var _this3 = this;
      return (0, _asyncToGenerator2.default)( /*#__PURE__*/_regenerator.default.mark(function _callee3() {
        var response, service;
        return _regenerator.default.wrap(function _callee3$(_context3) {
          while (1) {
            switch (_context3.prev = _context3.next) {
              case 0:
                _context3.prev = 0;
                _context3.next = 3;
                return _this3.$api.getLitterServicePage({
                  current: 1,
                  size: 10,
                  status: 'active'
                });
              case 3:
                response = _context3.sent;
                if (response.code === 0 && response.data && response.data.records && response.data.records.length > 0) {
                  service = response.data.records[0]; // 取第一个服务作为详情
                  _this3.serviceData = {
                    id: service.id,
                    name: service.name,
                    price: service.price,
                    bannerImage: service.imageUrl || '/static/images/banner1.jpg',
                    introduction: _this3.parseDescriptionToList(service.description),
                    instructions: ['购买后凭电子券码预约使用', '需提前2小时电话预约', '节假日价格可能浮动，详情请咨询客服']
                  };
                } else {
                  // 如果API没有数据，使用默认数据
                  _this3.loadDefaultLitterServiceData();
                }
                _context3.next = 11;
                break;
              case 7:
                _context3.prev = 7;
                _context3.t0 = _context3["catch"](0);
                console.error('加载铲屎服务数据失败:', _context3.t0);
                // 出错时使用默认数据
                _this3.loadDefaultLitterServiceData();
              case 11:
              case "end":
                return _context3.stop();
            }
          }
        }, _callee3, null, [[0, 7]]);
      }))();
    },
    loadDefaultLitterServiceData: function loadDefaultLitterServiceData() {
      this.serviceData = {
        id: 1,
        name: '上门铲屎服务',
        price: 50,
        bannerImage: '/static/images/banner1.jpg',
        introduction: ['适用宠物: 猫咪、狗狗', '适用宠物性别: 公母均可', '服务范围: 室内宠物生活区域清洁', '服务时长: 30-60分钟', '包含内容: 清理猫砂盆、清洁宠物用品、除味处理', '服务时间: 每日9:00-18:00', '需要提前2小时预约', '特殊要求请提前说明', '服务完成后提供清洁报告'],
        instructions: ['购买后凭电子券码预约使用', '需提前2小时电话预约', '节假日价格可能浮动，详情请咨询客服']
      };
    },
    loadBoardingServiceData: function loadBoardingServiceData() {
      var _this4 = this;
      return (0, _asyncToGenerator2.default)( /*#__PURE__*/_regenerator.default.mark(function _callee4() {
        var response, service;
        return _regenerator.default.wrap(function _callee4$(_context4) {
          while (1) {
            switch (_context4.prev = _context4.next) {
              case 0:
                _context4.prev = 0;
                _context4.next = 3;
                return _this4.$api.getBoardingServicePage({
                  current: 1,
                  size: 10,
                  status: 'active'
                });
              case 3:
                response = _context4.sent;
                if (response.code === 0 && response.data && response.data.records && response.data.records.length > 0) {
                  service = response.data.records[0]; // 取第一个服务作为详情
                  _this4.serviceData = {
                    id: service.id,
                    name: service.name,
                    price: service.price,
                    bannerImage: service.imageUrl || '/static/images/banner2.jpg',
                    introduction: _this4.parseDescriptionToList(service.description),
                    instructions: ['购买后凭电子券码预约使用', '需提前一天预约', '请提供宠物健康证明和疫苗接种记录']
                  };
                } else {
                  // 如果API没有数据，使用默认数据
                  _this4.loadDefaultBoardingServiceData();
                }
                _context4.next = 11;
                break;
              case 7:
                _context4.prev = 7;
                _context4.t0 = _context4["catch"](0);
                console.error('加载寄养服务数据失败:', _context4.t0);
                // 出错时使用默认数据
                _this4.loadDefaultBoardingServiceData();
              case 11:
              case "end":
                return _context4.stop();
            }
          }
        }, _callee4, null, [[0, 7]]);
      }))();
    },
    loadDefaultBoardingServiceData: function loadDefaultBoardingServiceData() {
      this.serviceData = {
        id: 2,
        name: '宠物寄养服务',
        price: 100,
        bannerImage: '/static/images/banner2.jpg',
        introduction: ['适用宠物: 猫咪、狗狗', '适用宠物性别: 公母均可', '寄养环境: 温馨舒适的独立空间', '寄养时长: 按天计算', '包含内容: 日常喂养、遛狗、清洁、陪伴', '专业护理: 24小时监控，专业护理人员', '健康保障: 定期健康检查', '个性化服务: 根据宠物习惯定制服务', '安全保障: 宠物保险覆盖'],
        instructions: ['购买后凭电子券码预约使用', '需提前一天预约', '请提供宠物健康证明和疫苗接种记录']
      };
    },
    loadMedicalServiceData: function loadMedicalServiceData() {
      var _this5 = this;
      return (0, _asyncToGenerator2.default)( /*#__PURE__*/_regenerator.default.mark(function _callee5() {
        var response, service;
        return _regenerator.default.wrap(function _callee5$(_context5) {
          while (1) {
            switch (_context5.prev = _context5.next) {
              case 0:
                _context5.prev = 0;
                _context5.next = 3;
                return _this5.$api.getMedicalServicePage({
                  current: 1,
                  size: 10,
                  status: 'active'
                });
              case 3:
                response = _context5.sent;
                if (response.code === 0 && response.data && response.data.records && response.data.records.length > 0) {
                  service = response.data.records[0]; // 取第一个服务作为详情
                  _this5.serviceData = {
                    id: service.id,
                    name: service.name,
                    price: service.price,
                    bannerImage: service.imageUrl || '/static/images/banner3.jpg',
                    introduction: _this5.parseDescriptionToList(service.description),
                    instructions: ['购买后凭电子券码预约使用', '需提前一天预约', '请携带宠物健康档案']
                  };
                } else {
                  // 如果API没有数据，使用默认数据
                  _this5.loadDefaultMedicalServiceData();
                }
                _context5.next = 11;
                break;
              case 7:
                _context5.prev = 7;
                _context5.t0 = _context5["catch"](0);
                console.error('加载医疗服务数据失败:', _context5.t0);
                // 出错时使用默认数据
                _this5.loadDefaultMedicalServiceData();
              case 11:
              case "end":
                return _context5.stop();
            }
          }
        }, _callee5, null, [[0, 7]]);
      }))();
    },
    loadDefaultMedicalServiceData: function loadDefaultMedicalServiceData() {
      this.serviceData = {
        id: 3,
        name: '宠物医疗服务',
        price: 200,
        bannerImage: '/static/images/banner3.jpg',
        introduction: ['适用宠物: 猫咪、狗狗', '适用宠物性别: 公母均可', '服务内容: 健康检查、疫苗接种、疾病治疗', '专业医师: 持证兽医，经验丰富', '医疗设备: 先进医疗设备，精准诊断', '服务时间: 每日8:00-20:00', '急诊服务: 24小时急诊热线', '药品供应: 宠物专用药品齐全', '后续跟踪: 治疗后定期回访'],
        instructions: ['购买后凭电子券码预约使用', '需提前一天预约', '请携带宠物健康档案']
      };
    },
    loadGroomingServiceData: function loadGroomingServiceData() {
      var _this6 = this;
      return (0, _asyncToGenerator2.default)( /*#__PURE__*/_regenerator.default.mark(function _callee6() {
        var response, service;
        return _regenerator.default.wrap(function _callee6$(_context6) {
          while (1) {
            switch (_context6.prev = _context6.next) {
              case 0:
                _context6.prev = 0;
                _context6.next = 3;
                return _this6.$api.getGroomingServicePage({
                  current: 1,
                  size: 10,
                  status: 'active'
                });
              case 3:
                response = _context6.sent;
                if (response.code === 0 && response.data && response.data.records && response.data.records.length > 0) {
                  service = response.data.records[0]; // 取第一个服务作为详情
                  _this6.serviceData = {
                    id: service.id,
                    name: service.name,
                    price: service.price,
                    bannerImage: service.imageUrl || '/static/images/banner1.jpg',
                    introduction: _this6.parseDescriptionToList(service.description),
                    instructions: ['购买后凭电子券码预约使用', '需提前一天预约', '请确保宠物身体健康']
                  };
                } else {
                  // 如果API没有数据，使用默认数据
                  _this6.loadDefaultGroomingServiceData();
                }
                _context6.next = 11;
                break;
              case 7:
                _context6.prev = 7;
                _context6.t0 = _context6["catch"](0);
                console.error('加载洗护服务数据失败:', _context6.t0);
                // 出错时使用默认数据
                _this6.loadDefaultGroomingServiceData();
              case 11:
              case "end":
                return _context6.stop();
            }
          }
        }, _callee6, null, [[0, 7]]);
      }))();
    },
    loadDefaultGroomingServiceData: function loadDefaultGroomingServiceData() {
      this.serviceData = {
        id: 4,
        name: '宠物洗护服务',
        price: 150,
        bannerImage: '/static/images/banner1.jpg',
        introduction: ['适用宠物: 猫咪、狗狗', '适用宠物性别: 公母均可', '服务内容: 洗澡、吹干、梳毛、剪指甲', '专业用品: 宠物专用洗护用品', '服务时长: 60-90分钟', '美容师: 专业宠物美容师', '造型设计: 根据宠物特点设计造型', '健康检查: 洗护过程中进行基础检查', '后续护理: 提供护理建议'],
        instructions: ['购买后凭电子券码预约使用', '需提前一天预约', '请确保宠物身体健康']
      };
    },
    loadAdoptionServiceData: function loadAdoptionServiceData() {
      var _this7 = this;
      return (0, _asyncToGenerator2.default)( /*#__PURE__*/_regenerator.default.mark(function _callee7() {
        var response, service;
        return _regenerator.default.wrap(function _callee7$(_context7) {
          while (1) {
            switch (_context7.prev = _context7.next) {
              case 0:
                _context7.prev = 0;
                _context7.next = 3;
                return _this7.$api.getAdoptionServicePage({
                  current: 1,
                  size: 10,
                  status: 'active'
                });
              case 3:
                response = _context7.sent;
                if (response.code === 0 && response.data && response.data.records && response.data.records.length > 0) {
                  service = response.data.records[0]; // 取第一个服务作为详情
                  _this7.serviceData = {
                    id: service.id,
                    name: service.name,
                    price: service.price,
                    bannerImage: service.imageUrl || '/static/images/banner2.jpg',
                    introduction: _this7.parseDescriptionToList(service.description),
                    instructions: ['领养完全免费', '需填写领养申请表', '通过审核后方可领养']
                  };
                } else {
                  // 如果API没有数据，使用默认数据
                  _this7.loadDefaultAdoptionServiceData();
                }
                _context7.next = 11;
                break;
              case 7:
                _context7.prev = 7;
                _context7.t0 = _context7["catch"](0);
                console.error('加载领养服务数据失败:', _context7.t0);
                // 出错时使用默认数据
                _this7.loadDefaultAdoptionServiceData();
              case 11:
              case "end":
                return _context7.stop();
            }
          }
        }, _callee7, null, [[0, 7]]);
      }))();
    },
    loadDefaultAdoptionServiceData: function loadDefaultAdoptionServiceData() {
      this.serviceData = {
        id: 5,
        name: '宠物领养服务',
        price: 0,
        bannerImage: '/static/images/banner2.jpg',
        introduction: ['适用宠物: 流浪猫、流浪狗', '领养条件: 有爱心、有责任心、有稳定住所', '领养流程: 申请-审核-面谈-领养', '健康保障: 已绝育、已疫苗、已体检', '后续支持: 提供饲养指导和医疗咨询', '领养协议: 签署领养协议，保障宠物权益', '回访服务: 定期回访，确保宠物生活状况', '终身服务: 提供终身饲养咨询', '爱心传递: 让更多流浪宠物找到温暖的家'],
        instructions: ['领养完全免费', '需填写领养申请表', '通过审核后方可领养']
      };
    },
    onBookNow: function onBookNow() {
      // 根据服务类型跳转到不同的预约页面
      var url = '';
      switch (this.serviceType) {
        case 'hospital':
          url = '/pages/appointment/book-hospital';
          break;
        case 'litter':
          url = '/pages/appointment/book-door-cleaning';
          break;
        case 'boarding':
          url = '/pages/appointment/boarding';
          break;
        case 'medical':
          url = '/pages/appointment/medical';
          break;
        case 'grooming':
          url = '/pages/appointment/grooming';
          break;
        case 'adoption':
          url = '/pages/appointment/adoption';
          break;
      }
      if (url) {
        uni.navigateTo({
          url: url
        });
      } else {
        // 跳转到预约页面
        uni.navigateTo({
          url: "/pages/booking/index?serviceType=".concat(this.serviceType, "&serviceId=").concat(this.serviceId)
        });
      }
    },
    onBannerError: function onBannerError(e) {
      console.log('展示图加载失败:', e);
      // 设置默认图片
      this.serviceData.bannerImage = '/static/images/default-banner.jpg';
    },
    // 处理图片URL，解决小程序HTTP协议限制问题
    getImageUrl: function getImageUrl(imageUrl) {
      return _util.util.getImageUrl(imageUrl);
    },
    // 解析描述文本为列表
    parseDescriptionToList: function parseDescriptionToList(description) {
      if (!description) {
        return [];
      }

      // 如果描述包含换行符，按换行分割
      if (description.includes('\n')) {
        return description.split('\n').filter(function (item) {
          return item.trim();
        });
      }

      // 如果描述包含分号，按分号分割
      if (description.includes(';')) {
        return description.split(';').filter(function (item) {
          return item.trim();
        });
      }

      // 如果描述包含句号，按句号分割
      if (description.includes('。')) {
        return description.split('。').filter(function (item) {
          return item.trim();
        });
      }

      // 否则返回单个描述
      return [description];
    }
  }
};
exports.default = _default;
/* WEBPACK VAR INJECTION */}.call(this, __webpack_require__(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)["default"]))

/***/ }),

/***/ 320:
/*!************************************************************************************************************************************************!*\
  !*** C:/Users/Yu/Desktop/pet-home/pet-home-uniapp/pages/service/hospital-detail.vue?vue&type=style&index=0&id=5d3fc6fe&lang=scss&scoped=true& ***!
  \************************************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _D_HBuilderX_plugins_uniapp_cli_node_modules_mini_css_extract_plugin_dist_loader_js_ref_8_oneOf_1_0_D_HBuilderX_plugins_uniapp_cli_node_modules_css_loader_dist_cjs_js_ref_8_oneOf_1_1_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_loaders_stylePostLoader_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_8_oneOf_1_2_D_HBuilderX_plugins_uniapp_cli_node_modules_postcss_loader_src_index_js_ref_8_oneOf_1_3_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_sass_loader_dist_cjs_js_ref_8_oneOf_1_4_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_8_oneOf_1_5_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_hospital_detail_vue_vue_type_style_index_0_id_5d3fc6fe_lang_scss_scoped_true___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--8-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--8-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--8-oneOf-1-2!./node_modules/postcss-loader/src??ref--8-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/sass-loader/dist/cjs.js??ref--8-oneOf-1-4!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--8-oneOf-1-5!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./hospital-detail.vue?vue&type=style&index=0&id=5d3fc6fe&lang=scss&scoped=true& */ 321);
/* harmony import */ var _D_HBuilderX_plugins_uniapp_cli_node_modules_mini_css_extract_plugin_dist_loader_js_ref_8_oneOf_1_0_D_HBuilderX_plugins_uniapp_cli_node_modules_css_loader_dist_cjs_js_ref_8_oneOf_1_1_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_loaders_stylePostLoader_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_8_oneOf_1_2_D_HBuilderX_plugins_uniapp_cli_node_modules_postcss_loader_src_index_js_ref_8_oneOf_1_3_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_sass_loader_dist_cjs_js_ref_8_oneOf_1_4_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_8_oneOf_1_5_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_hospital_detail_vue_vue_type_style_index_0_id_5d3fc6fe_lang_scss_scoped_true___WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_D_HBuilderX_plugins_uniapp_cli_node_modules_mini_css_extract_plugin_dist_loader_js_ref_8_oneOf_1_0_D_HBuilderX_plugins_uniapp_cli_node_modules_css_loader_dist_cjs_js_ref_8_oneOf_1_1_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_loaders_stylePostLoader_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_8_oneOf_1_2_D_HBuilderX_plugins_uniapp_cli_node_modules_postcss_loader_src_index_js_ref_8_oneOf_1_3_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_sass_loader_dist_cjs_js_ref_8_oneOf_1_4_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_8_oneOf_1_5_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_hospital_detail_vue_vue_type_style_index_0_id_5d3fc6fe_lang_scss_scoped_true___WEBPACK_IMPORTED_MODULE_0__);
/* harmony reexport (unknown) */ for(var __WEBPACK_IMPORT_KEY__ in _D_HBuilderX_plugins_uniapp_cli_node_modules_mini_css_extract_plugin_dist_loader_js_ref_8_oneOf_1_0_D_HBuilderX_plugins_uniapp_cli_node_modules_css_loader_dist_cjs_js_ref_8_oneOf_1_1_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_loaders_stylePostLoader_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_8_oneOf_1_2_D_HBuilderX_plugins_uniapp_cli_node_modules_postcss_loader_src_index_js_ref_8_oneOf_1_3_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_sass_loader_dist_cjs_js_ref_8_oneOf_1_4_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_8_oneOf_1_5_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_hospital_detail_vue_vue_type_style_index_0_id_5d3fc6fe_lang_scss_scoped_true___WEBPACK_IMPORTED_MODULE_0__) if(["default"].indexOf(__WEBPACK_IMPORT_KEY__) < 0) (function(key) { __webpack_require__.d(__webpack_exports__, key, function() { return _D_HBuilderX_plugins_uniapp_cli_node_modules_mini_css_extract_plugin_dist_loader_js_ref_8_oneOf_1_0_D_HBuilderX_plugins_uniapp_cli_node_modules_css_loader_dist_cjs_js_ref_8_oneOf_1_1_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_loaders_stylePostLoader_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_8_oneOf_1_2_D_HBuilderX_plugins_uniapp_cli_node_modules_postcss_loader_src_index_js_ref_8_oneOf_1_3_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_sass_loader_dist_cjs_js_ref_8_oneOf_1_4_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_8_oneOf_1_5_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_hospital_detail_vue_vue_type_style_index_0_id_5d3fc6fe_lang_scss_scoped_true___WEBPACK_IMPORTED_MODULE_0__[key]; }) }(__WEBPACK_IMPORT_KEY__));
 /* harmony default export */ __webpack_exports__["default"] = (_D_HBuilderX_plugins_uniapp_cli_node_modules_mini_css_extract_plugin_dist_loader_js_ref_8_oneOf_1_0_D_HBuilderX_plugins_uniapp_cli_node_modules_css_loader_dist_cjs_js_ref_8_oneOf_1_1_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_loaders_stylePostLoader_js_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_8_oneOf_1_2_D_HBuilderX_plugins_uniapp_cli_node_modules_postcss_loader_src_index_js_ref_8_oneOf_1_3_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_sass_loader_dist_cjs_js_ref_8_oneOf_1_4_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_8_oneOf_1_5_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_D_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_hospital_detail_vue_vue_type_style_index_0_id_5d3fc6fe_lang_scss_scoped_true___WEBPACK_IMPORTED_MODULE_0___default.a); 

/***/ }),

/***/ 321:
/*!****************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
  !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--8-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--8-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--8-oneOf-1-2!./node_modules/postcss-loader/src??ref--8-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/sass-loader/dist/cjs.js??ref--8-oneOf-1-4!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--8-oneOf-1-5!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!C:/Users/Yu/Desktop/pet-home/pet-home-uniapp/pages/service/hospital-detail.vue?vue&type=style&index=0&id=5d3fc6fe&lang=scss&scoped=true& ***!
  \****************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

// extracted by mini-css-extract-plugin
    if(false) { var cssReload; }
  

/***/ })

},[[314,"common/runtime","common/vendor"]]]);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/service/hospital-detail.js.map