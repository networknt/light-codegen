/**
 * Created by Nicholas Azar on 2017-07-05.
 */

import React, {Component} from 'react';
import {Row, Col, Button, Icon} from 'antd';
import SelectConfigForm from "./select-config-form/SelectConfigForm";
import PropTypes from 'prop-types';

class SelectConfigStep extends Component {

    onFormInit = (initForm) => {
        if (initForm) {
            this.setState({
                configForm: initForm.props.form
            });
        }
    };

    /**
     * When the next button is clicked, validate the fields, and if they're valid, move on.
     * If they're not, the validateFields function will take care of triggering the messages.
     */
    onNextClick = () => {
        this.state.configForm.validateFields((err, values) => {
            if (!err) {
                this.props.onNextClick();
            }
        });
    };

    /**
     * When the form changes, dispatch event to the controller to maintain state.
     *
     * @param change
     */
    onChange = (change) => {
        if (change.configFiles && change.configFiles.length > 0) {
            change.configContent = JSON.stringify(change.configFiles[0].response, null, 2)
        }
        this.props.onChange(change);
    };

    render() {
        return (
            <div style={{padding: '20px'}}>
                <Row type="flex" justify="center">
                    <Col xs={24} sm={18} md={14} lg={12} xl={6}>
                        <h2 style={{color: 'rgba(0, 0, 0, 0.5)'}}>Config</h2>
                        <p>
                            Select a configuration file for the generator.
                        </p>
                    </Col>
                </Row>
                <div style={{height: '20px'}}/>
                <Row type="flex" justify="center">
                    <Col xs={24} sm={18} md={14} lg={12} xl={6}>
                        <SelectConfigForm onChange={this.onChange} wrappedComponentRef={this.onFormInit} initValues={this.props.initValues} />
                    </Col>
                </Row>
                <Row type="flex">
                    <Col span={2}>
                        <Button type="primary" shape="circle" className="nav-button prev" onClick={this.props.onPrevClick}>
                            <Icon type="arrow-left" style={{fontSize: 20, paddingTop: '4px'}}/>
                        </Button>
                    </Col>
                    <Col span={2} offset={20}>
                        <Button type="primary" shape="circle" className="nav-button next" onClick={this.onNextClick}>
                            <Icon type="arrow-right" style={{fontSize: 20, paddingTop: '4px'}}/>
                        </Button>
                    </Col>
                </Row>
            </div>
        )
    }
}

SelectConfigStep.PropTypes = {
    onPrevClick: PropTypes.func.isRequired,
    onNextClick: PropTypes.func.isRequired
};

export default SelectConfigStep;