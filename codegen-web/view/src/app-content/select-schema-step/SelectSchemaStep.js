/**
 * Created by Nicholas Azar on 2017-07-04.
 */

import React, {Component} from 'react';
import {Row, Col, Button, Icon} from 'antd';
import SelectSchemaForm from "./select-schema-form/SelectSchemaForm";


class SelectSchemaStep extends Component {

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
                        <SelectSchemaForm />
                    </Col>
                </Row>
                <Row type="flex" justify="right">
                    <Col span={2} offset={22}>
                        <Button type="primary" shape="circle" style={{width: '50px', height: '50px', backgroundColor: '#00bfa5', border: 'None'}}>
                            <Icon type="arrow-right" style={{fontSize: 20, paddingTop: '4px'}}/>
                        </Button>
                    </Col>
                </Row>
            </div>
        )
    }
}

export default SelectSchemaStep;