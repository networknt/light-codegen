/**
 * Created by Nicholas Azar on 2017-07-04.
 */

import React, {Component} from "react";
import {Button, Col, Icon, Row} from "antd";
import SelectSchemaForm from "./select-schema-form/SelectSchemaForm";


class SelectSchemaStep extends Component {

    onFormInit = (initForm) => {
        if (initForm) {
            this.setState({
                schemaForm: initForm.props.form
            });
        }
    };

    onNextClick = () => {
        this.state.schemaForm.validateFields((err, values) => {
            if (!err) {
                this.props.onNextClick();
            }
        });
    };

    onChange = (change) => {
        this.props.onChange(change);
    };

    render() {
        return (
            <div style={{padding: '20px'}}>
                <Row type="flex" justify="center">
                    <Col xs={24} sm={18} md={14} lg={12} xl={6}>
                        <h2 style={{color: 'rgba(0, 0, 0, 0.5)'}}>Schema</h2>
                        <p>
                            Select a generator and upload the corresponding API schema.
                        </p>
                    </Col>
                </Row>
                <div style={{height: '20px'}}/>
                <Row type="flex" justify="center">
                    <Col xs={24} sm={18} md={14} lg={12} xl={6}>
                        <SelectSchemaForm onChange={this.onChange} wrappedComponentRef={this.onFormInit} initValues={this.props.initValues} />
                    </Col>
                </Row>
                <Row type="flex" justify="right">
                    <Col span={2} offset={22}>
                        <Button type="primary" shape="circle" className="nav-button next" onClick={this.onNextClick}>
                            <Icon type="arrow-right" style={{fontSize: 20, paddingTop: '4px'}}/>
                        </Button>
                    </Col>
                </Row>
            </div>
        )
    }
}

export default SelectSchemaStep;