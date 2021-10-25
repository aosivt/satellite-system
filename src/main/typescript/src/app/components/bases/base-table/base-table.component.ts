import {AfterViewInit, Component, ElementRef, Inject, Input, OnInit, ViewChild} from '@angular/core';
// import {MainTabComponent} from "../../main-tab/main-tab.component";
import {MatSort, MatSortable, Sort} from "@angular/material/sort";
import {MatTable, MatTableDataSource} from "@angular/material/table";
import {MatPaginator} from "@angular/material/paginator";
import {ColumnsOptions, ColumnsOptionsType} from "../../../types/columns-options";
// import {BaseTableRestClient} from "../../../services/base-table-service/base-table-rest-client";
import {MatCheckbox} from "@angular/material/checkbox";
import {MatDialog} from "@angular/material/dialog";
import {formatDate} from "@angular/common";
import {FabricMessage} from "../messages/fabric-message";
import {DialogErrorData} from "../messages/dialog-error-message/data/dialog-error-data";
import * as _ from "lodash";
import {environment} from "../../../../environments/environment";



let DEFAULT_OFFSET: number = 25;

@Component({
  selector: 'base-table',
  templateUrl: 'base-table.component.html',
  styleUrls: ['base-table.component.css']
})
export class BaseTableComponent<T> implements OnInit, AfterViewInit {
  data: T[] = [];
  dataSource: MatTableDataSource<T> | undefined;

  public static readonly DEFAULT_OFFSET: number = 25;
  public static readonly START_DEFAULT_OFFSET: number = 0;

  @Input() isLocal: boolean = false;
  @Input() disabled: boolean = false;

  @Input() columns: ColumnsOptions[]  = [];
  @Input() nameEntityService: string = "";
  @Input() checkErrorRow: Function = (item: T) => {
  };
  @Input() checkOther: Function = (item: T) => {
  };
  // @ts-ignore
  @Input() customTableFilterPredicate: ((data: T, filter: string) => boolean) = null;

  selectedEntities: T[] = [];

  @Input() dblClickFunctionsCell: Function[] = [];
  @Input() afterUpdateDateFunction: Function[] = [];
  @Input() afterClickCellFunction: Function[] = [];

  @Input() tdTableClassName: Function = (item: T) => {
    return ['']
  };

  readonly functionClickCell: Function = (event: Event, row: T, columnItem: ColumnsOptions) => {
    // @ts-ignore
    const withCtrl = event['ctrlKey'];
    // @ts-ignore
    const withShift = event['shiftKey'];
    if (!environment.production){
      console.log(`> call functionClickCell with shift button: ${withShift}; or with ctrl button: ${withCtrl}`);
    }

    if (withCtrl) {
      if (_.findIndex(this.selectedEntities, row) !== -1) return;
      this.selectedEntities.push(row);
    } else if (withShift) {
      const insertedRowIndex = _.findIndex(this.data, row);
      this.selectedEntities.push(row);
      const previewsRowIndex = _.findIndex(this.data, this.selectedEntities[this.selectedEntities.length - 2]);
      const fundedSelectedEntities =
        this.data.filter((t, ind) => {
          return previewsRowIndex < insertedRowIndex ? previewsRowIndex <= ind && ind <= insertedRowIndex :
            insertedRowIndex <= ind && ind <= previewsRowIndex;
        });
      fundedSelectedEntities.forEach(t =>{
        if (_.findIndex(this.selectedEntities, t) === -1){
          this.selectedEntities.push(t);
        }
      });

    } else {
      this.selectedEntities = [];
      this.selectedEntities.push(row);
    }
  };

  public constructor(
    // public mainTab: MainTabComponent,
    //                  public service: BaseTableRestClient<T>,
                     public dialog: MatDialog) {
    this.offset = BaseTableComponent.START_DEFAULT_OFFSET;

  }

  isLoadingResults = true;
  filterObject: {} = {};
  sorts: Sort[] = [];
  offset: number = BaseTableComponent.START_DEFAULT_OFFSET;

  countClickCheckBox: number = 0;

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator | undefined;
  @ViewChild(MatSort, {static: false}) sort: MatSort| undefined;
  @ViewChild('filter', {static: true}) filter: ElementRef| undefined;
  @ViewChild(MatTable) basetable: MatTable<T>| undefined;

  ngOnInit(): void {
    this.afterClickCellFunction.push(this.functionClickCell);
  }

  ngAfterViewInit() {
    if (this.isLocal) this.updateTable();

  }

  public clickT(): void {

  }

  public alert(item: T): void {
    console.log(item);
  }

  public save(event: Event, row: T, columnItem: ColumnsOptions) {
    columnItem.isEdit = false;
    // let name = this.nameEntityService === undefined ?
    //   this.mainTab.getActiveTab().treeNode.name : this.nameEntityService;
    // this.service.save(name, row).subscribe(value => {
    //   },
    //   error => {
    //     FabricMessage.showDialogError({
    //       title: 'Ошибка обработки:' + error['name'],
    //       message: error['message'],
    //       httpStatus: error['status']
    //     } as DialogErrorData);
    //   }
    // );
  }

  public selectItem(item: T, columnItem: ColumnsOptions): void {
    // this.selectedEntities = [];
    this.selectedEntities.push(item);
    this.dblClickFunctionsCell.forEach(f => f(item, columnItem));
  }

  public sortColumn(sort: Sort): void {
    if (this.isLocal) {
      // @ts-ignore
      this.sort.active = sort.active;
      // @ts-ignore
      this.sort.direction = sort.direction;
      // @ts-ignore
      this.sort.sortChange.emit(sort);
      return;
    }
    this.sorts = [];
    this.sorts.push(sort);
    this.isLoadingResults = true;
    this.offset = BaseTableComponent.START_DEFAULT_OFFSET;
    this.updateTable();
  }

  public async startFiltering(co: ColumnsOptions, value: any, isForceUpdate?: boolean, isReport?: boolean): Promise<void> {
    if (!environment.production){
      console.log(`> call startFiltering is force update : ${isForceUpdate} `)
      console.log(`> call startFiltering is report (is get all) update : ${isReport} `)
    }
    let key = co.metaFieldName === undefined ? co.key : co.metaFieldName;
    // @ts-ignore
    this.filterObject[key] = value === '' ? null : value;
    this.offset = BaseTableComponent.START_DEFAULT_OFFSET;
    await this.updateTable(isReport, isForceUpdate,);
  }

  public startFilteringByArrayColumnsOptions(collectionColumnsOptions: ColumnsOptions[], value: any[], isForceUpdate?: boolean, isReport?: boolean): void {
    if (!environment.production){
      console.log(`> call startFilteringByArrayColumnsOptions is force update : ${isForceUpdate} `)
      console.log(`> call startFilteringByArrayColumnsOptions is report (is get all) update : ${isReport} `)
    }
    this.filterObject = {};
    collectionColumnsOptions.forEach((f, i) => {
      let key = f.metaFieldName === undefined ? f.key : f.metaFieldName;
      // @ts-ignore
      this.filterObject[key] = value[i] === '' ? null : value[i];
    });
    this.offset = BaseTableComponent.START_DEFAULT_OFFSET;
    this.updateTable(isReport, isForceUpdate);
  }

  public startFilteringDate(event: Event, columnItem: ColumnsOptions, value: any): void {
    event.stopPropagation();
    if (value === undefined || value === null) {
      this.startFiltering(columnItem, '');
      return;
    }
    let formatAction = columnItem.formatDate;
    const format = formatAction === undefined ? null : formatAction(null, null, columnItem);
    const _value = format === null ? value : formatDate(value, format, 'en-US');
    this.startFiltering(columnItem, _value);
  }

  public clickCell(event: Event, row: T, columnItem: ColumnsOptions) {
    // @ts-ignore
    columnItem.clickCell(event, row, columnItem);
    this.afterClickCellFunction.forEach(f => f(event, row, columnItem));
  }

  onChangeCheckBox(co: ColumnsOptions, checkbox: MatCheckbox): void {
    console.log(checkbox.indeterminate);
    // @ts-ignore
    if (checkbox.checked && !checkbox.indeterminate && this.filterObject[co.key] != null) {
      checkbox.indeterminate = true;
      this.startFiltering(co, null);
      return;
    } else { // @ts-ignore
      if (!this.filterObject[co.key]) {
            checkbox.checked = true;
          }
    }

    this.startFiltering(co, String(checkbox.checked));
  }

  public scrollTable(scrollEvent: any) {
    if (!environment.production){
      console.log(`> call scrollTable is force update : ${this.offset + DEFAULT_OFFSET} `)
    }
    if (this.isLocal) return;
    let scrollTop: number = scrollEvent['srcElement']['scrollTop'];
    let scrollHeight: number = scrollEvent['srcElement']['scrollHeight'];
    let clientHeight: number = scrollEvent['srcElement']['clientHeight'];
    let needAddItem = (scrollHeight - (scrollTop + clientHeight)) === 0;
    if (needAddItem) {
      this.offset = this.offset + DEFAULT_OFFSET;
      this.updateTableWithAdd();
    }
  }

  public async updateTable(isReportQuery?: boolean, isForceUpdate?: boolean): Promise<void> {
    if (!environment.production){
      console.log(`> call updateTable is force update : ${isForceUpdate} `)
    }
    if (this.isLocal && !(isForceUpdate !== undefined && isForceUpdate)) return;
    const _this = this;
    const isReport = (isReportQuery === undefined || isReportQuery === null) ? false : isReportQuery;
    // let name = this.nameEntityService === undefined ?
      // this.mainTab.getActiveTab().treeNode.name : this.nameEntityService;
    // await this.service.search(name, this._sorts, this.filterObject, this.offset, isReport)
    //   .subscribe(result => _this.updateData(result),
    //     error =>
    //       FabricMessage.showDialogError({
    //         title: 'Ошибка обработки:' + error['name'],
    //         message: error['message'],
    //         httpStatus: error['status']
    //       } as DialogErrorData));
  }

  public updateTableWithAdd() {
    // let name = this.nameEntityService === undefined ?
    //   this.mainTab.getActiveTab().treeNode.name : this.nameEntityService;
    // this.service.search(name, this._sorts, this.filterObject, this.offset)
    //   .subscribe(result => this.updateData(this.data.concat(result)),
    //     error =>
    //       FabricMessage.showDialogError({
    //         title: 'Ошибка обработки:' + error['name'],
    //         message: error['message'],
    //         httpStatus: error['status']
    //       } as DialogErrorData));
  }

  public updateData(result: T[], executeAfterUpdateFunction?: boolean) {
    if (!environment.production){
      console.log(`> call updateData base-table with resutl length: ${result.length} `)
    }

    if (result === null || result === undefined) return;

    this.isLoadingResults = false;
    this.data = result;
    this.dataSource = new MatTableDataSource<T>(this.data);
    if (this.isLocal) { // @ts-ignore
      this.dataSource.sort = this.sort;
    }
    if (executeAfterUpdateFunction === undefined) {
      this.afterUpdateDateFunction.forEach(f => f());
    } else if (executeAfterUpdateFunction) {
      this.afterUpdateDateFunction.forEach(f => f());
    }
    if (this.customTableFilterPredicate !== null){
      this.dataSource.filterPredicate = this.customTableFilterPredicate;
    }
  }
  public refreshViewData(){
    this.dataSource = new MatTableDataSource<T>(this.data);
  }
  applyLocalFilter(event: Event): void {
    if (!environment.production){
      console.log(`> call applyLocalFilter base-table `)
    }
    const filterValue = (event.target as HTMLInputElement).value;
    if (!environment.production){
      console.log(`> call applyLocalFilter base-table, filterValue: ${filterValue} `)
      console.log(`> call applyLocalFilter base-table, filterValue trimed: ${filterValue.trim().toLowerCase()} `)
    }
    // @ts-ignore
    this.dataSource.filter = filterValue.trim().toLowerCase();
    // @ts-ignore
    if (this.dataSource.paginator) {
      // @ts-ignore
      this.dataSource.paginator.firstPage();
    }
  }


  public refreshTable() {
    // Refreshing table using paginator
    // Thanks yeager-j for tips
    // https://github.com/marinantonio/angular-mat-table-crud/issues/12
    // this.paginator._changePageSize(this.paginator.pageSize);
    // this.basetable.renderRows();
  }

  clickCheckBox(event: Event) {
    this.countClickCheckBox += 1;
    if (this.countClickCheckBox > 2) {
      this.countClickCheckBox = 0;
    }
    console.log(this.countClickCheckBox);
    event.stopPropagation();
  }



  get _sorts(): Sort[] | null {
    return this.sorts.length > 0 ? this.sorts : null;
  }

  get _displayColumns(): string[] {
    // @ts-ignore
    return this.columns.map(m => m.key);
  }

  // @ts-ignore
  get _columns(): ColumnsOptions[] {
    return this.columns === undefined ? [] : this.columns;
  }

  get moreThanOneSelectedEntities(): boolean {
    return this.selectedEntities.length > 1;
  }

  isSelectedItem(item: T) {
    return this.selectedEntities.includes(item);
  }

  set _columns(_columns: ColumnsOptions[]) {
    this.columns = _columns;
  }

  get BUTTON_TYPE() {
    return ColumnsOptionsType.BUTTON_TYPE;
  }

  get DEFAULT_TYPE() {
    return ColumnsOptionsType.LABEL_TYPE;
  }

  get TEXT_TYPE() {
    return ColumnsOptionsType.STRING_TYPE;
  }

  get BOOLEAN_TYPE() {
    return ColumnsOptionsType.BOOLEAN_TYPE;
  }

  get LABEL_TYPE() {
    return ColumnsOptionsType.LABEL_TYPE;
  }

  get ENUM_TYPE() {
    return ColumnsOptionsType.ENUM_TYPE;
  }

  get ENTITY_TYPE() {
    return ColumnsOptionsType.ENTITY_TYPE;
  }

  get CUSTOM_TYPE() {
    return ColumnsOptionsType.STRING_TYPE;
  }

  get DATE_TYPE() {
    return ColumnsOptionsType.DATE_TYPE;
  }

  get DATETIME_TYPE() {
    return ColumnsOptionsType.DATETIME_TYPE;
  }
}
